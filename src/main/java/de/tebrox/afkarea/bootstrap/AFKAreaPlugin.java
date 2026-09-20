package de.tebrox.afkarea.bootstrap;

import de.tebrox.afkarea.activity.ActivityListener;
import de.tebrox.afkarea.activity.ActivityService;
import de.tebrox.afkarea.activity.IdleTracker;
import de.tebrox.afkarea.area.*;
import de.tebrox.afkarea.area.selection.SelectionService;
import de.tebrox.afkarea.command.AFKAreaCommands;
import de.tebrox.afkarea.command.AfkCommand;
import de.tebrox.afkarea.config.AFKAreaConfig;
import de.tebrox.afkarea.config.AFKAreaConfigValidator;
import de.tebrox.afkarea.config.ConfigValidationResult;
import de.tebrox.afkarea.config.MessageConfig;
import de.tebrox.afkarea.display.*;
import de.tebrox.afkarea.household.HouseholdData;
import de.tebrox.afkarea.household.HouseholdManager;
import de.tebrox.afkarea.integration.PlaceholderApiIntegration;
import de.tebrox.afkarea.integration.WorldGuardIntegration;
import de.tebrox.afkarea.integration.placeholder.AFKAreaPlaceholderExpansion;
import de.tebrox.afkarea.message.MessageService;
import de.tebrox.afkarea.persistence.AFKAreaDatabaseSettings;
import de.tebrox.afkarea.reward.RewardService;
import de.tebrox.afkarea.reward.RewardSessionService;
import de.tebrox.afkarea.session.AFKSessionData;
import de.tebrox.afkarea.session.AFKSessionHistoryService;
import de.tebrox.afkarea.state.PlayerState;
import de.tebrox.afkarea.state.PlayerStateService;
import de.tebrox.afkarea.stats.AFKPlayerStatsData;
import de.tebrox.afkarea.stats.AFKPlayerStatsService;
import de.tebrox.vertexCore.VertexCoreApi;
import de.tebrox.vertexCore.config.Config;
import de.tebrox.vertexCore.database.Database;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;


public final class AFKAreaPlugin extends JavaPlugin {
    private static final int BSTATS_PLUGIN_ID = 34037;

    private Config<AFKAreaConfig> configFile;
    private AFKAreaConfig config;
    private DatabaseConfigSnapshot activeDatabaseConfig;

    private Config<MessageConfig> messageFile;
    private MessageConfig messages;

    private MessageService messageService;

    private PlayerStateService playerStateService;
    private ActivityService activityService;
    private IdleTracker idleTracker;

    private TabListService tabListService;

    private Database<AreaData> areaDatabase;
    private AreaManager areaManager;

    private SelectionService selectionService;

    private AreaSessionService areaSessionService;
    private AreaVisibilityService visibilityService;
    private AreaTeleportService areaTeleportService;

    private RewardService rewardService;
    private RewardSessionService rewardSessionService;

    private Database<HouseholdData> householdDatabase;
    private HouseholdManager householdManager;

    private Database<AFKSessionData> sessionDatabase;
    private AFKSessionHistoryService sessionHistoryService;

    private Database<AFKPlayerStatsData> playerStatsDatabase;
    private AFKPlayerStatsService playerStatsService;

    private WorldGuardIntegration worldGuardIntegration;
    private PlaceholderApiIntegration placeholderApiIntegration;

    private BossBarDisplayService bossBarDisplayService;
    private ActionBarDisplayService actionBarDisplayService;

    @Override
    public void onEnable() {
        worldGuardIntegration = WorldGuardIntegration.detect(this);
        placeholderApiIntegration = PlaceholderApiIntegration.detect(this);

        if(worldGuardIntegration.isAvailable()) {
            getLogger().info("WorldGuard integration is available");
        }else{
            getLogger().info("WorldGuard not found - Worldguard integration is disabled");
        }

        configFile = new Config<>(this, AFKAreaConfig.class);
        AFKAreaConfig loadedConfig;

        try {
            loadedConfig = configFile.loadConfigObject();
        }catch(RuntimeException exception) {
            getLogger().severe("Failed to load config.yml: " + exception.getMessage());
            throw exception;
        }
        if(!validateConfiguration(loadedConfig)) {
            throw new IllegalStateException("AFKArea configuration is invalid. Check the previous log messages.");
        }
        config = loadedConfig;
        activeDatabaseConfig = DatabaseConfigSnapshot.from(config);

        AFKAreaDatabaseSettings databaseSettings = new AFKAreaDatabaseSettings(config);
        areaDatabase = new Database<>(this, databaseSettings, AreaData.class);
        householdDatabase = new Database<>(this, databaseSettings, HouseholdData.class);
        sessionDatabase = new Database<>(this, databaseSettings, AFKSessionData.class);
        playerStatsDatabase = new Database<>(this, databaseSettings, AFKPlayerStatsData.class);

        areaManager = new AreaManager(this, areaDatabase, worldGuardIntegration);
        householdManager = new HouseholdManager(this, householdDatabase);
        sessionHistoryService = new AFKSessionHistoryService(this, sessionDatabase);
        playerStatsService = new AFKPlayerStatsService(this, playerStatsDatabase);

        areaTeleportService = new AreaTeleportService(areaManager);

        messageFile = new Config<>(this, MessageConfig.class);
        messages = messageFile.loadConfigObject();

        messageService = new MessageService(() -> messages);

        playerStateService = new PlayerStateService();
        activityService = new ActivityService();

        tabListService = new TabListService(() -> messages, messageService);
        visibilityService = new AreaVisibilityService(this, playerStateService, () -> config, tabListService);

        rewardService = new RewardService(this, () -> messages, messageService);
        rewardSessionService = new RewardSessionService(this, areaManager, rewardService, () -> config, householdManager);

        bossBarDisplayService = new BossBarDisplayService(this, rewardSessionService, () -> config, () -> messages, messageService);
        actionBarDisplayService = new ActionBarDisplayService(this, rewardSessionService, () -> config, () -> messages, messageService);

        areaSessionService = new AreaSessionService(areaManager, playerStateService, activityService, tabListService, () -> messages, messageService, visibilityService, rewardSessionService, sessionHistoryService, playerStatsService);
        areaManager.setRuntimeChangeListener(() -> getServer().getOnlinePlayers().forEach(areaSessionService::sync));
        getServer().getPluginManager().registerEvents(new AreaSessionListener(areaSessionService), this);
        getServer().getPluginManager().registerEvents(new AreaVisibilityListener(this, visibilityService), this);

        areaManager.loadAsync();
        householdManager.loadAsync();
        playerStatsService.loadAsync();

        selectionService = new SelectionService();
        rewardSessionService.start();
        bossBarDisplayService.start();
        actionBarDisplayService.start();

        idleTracker = new IdleTracker(this, activityService, playerStateService, () -> config, () -> messages, messageService, tabListService, areaTeleportService, areaSessionService);
        idleTracker.start();

        getServer().getPluginManager().registerEvents(new ActivityListener(this, activityService, playerStateService, () -> messages, messageService, tabListService), this);

        getServer().getOnlinePlayers().forEach(player -> activityService.track(player.getUniqueId())
        );

        VertexCoreApi.get().commands().register(this, new AFKAreaCommands(this));
        VertexCoreApi.get().commands().register(this, new AfkCommand(this));

        if(placeholderApiIntegration.isAvailable()) {
            new AFKAreaPlaceholderExpansion(this).register();
            getLogger().info("PlaceholderAPI integration is available");
        }else{
            getLogger().info("PlaceholderAPI not found - PlaceholderAPI integration is disabled");
        }

        new Metrics(this, BSTATS_PLUGIN_ID);
        getLogger().info("AFKArea has been enabled");
    }

    @Override
    public void onDisable() {
        if(idleTracker != null) idleTracker.stop();
        if(actionBarDisplayService != null) actionBarDisplayService.stop();
        if(bossBarDisplayService != null) bossBarDisplayService.stop();
        if(rewardSessionService != null) rewardSessionService.stop();

        VertexCoreApi.get().commands().unregisterAll(this);

        if(visibilityService != null) visibilityService.restoreAll(getServer().getOnlinePlayers());
        if(tabListService != null) tabListService.restoreAll(getServer().getOnlinePlayers());
        if(selectionService != null) selectionService.clearAll();
        if(areaSessionService != null) areaSessionService.clearAll();
        if(playerStateService != null) playerStateService.clearAll();
        if(activityService != null) activityService.clearAll();
        if(householdManager != null) householdManager.clear();
        if(playerStatsService != null) playerStatsService.clear();
        if(areaManager != null) areaManager.clear();
        if(areaDatabase != null) areaDatabase.close();

        getLogger().info("AFKArea has been disabled");
    }

    public boolean reloadConfigs() {
        AFKAreaConfig reloadedConfig;
        MessageConfig reloadedMessages;

        try {
            reloadedConfig = configFile.loadConfigObject();
            reloadedMessages = messageFile.loadConfigObject();
        }catch(RuntimeException exception) {
            getLogger().severe("Failed to reload AFKArea configuration: " + exception.getMessage());
            return false;
        }

        if(!validateConfiguration(reloadedConfig)) {
            getLogger().severe("Configuration reload rejected. The previous configuration remains active.");
            return false;
        }

        boolean databaseConfigChanged = !activeDatabaseConfig.equals(DatabaseConfigSnapshot.from(reloadedConfig));
        if(databaseConfigChanged) getLogger().warning("Database configuration changed during reload. Database settings are initialized at startup and the changes will take effect after a server restart.");

        config = reloadedConfig;
        messages = reloadedMessages;

        bossBarDisplayService.refreshAll();
        actionBarDisplayService.refreshAll();
        visibilityService.refreshAll(getServer().getOnlinePlayers());

        areaManager.loadAsync();

        getServer().getOnlinePlayers().stream()
                .filter(player -> playerStateService.getState(player.getUniqueId()) == PlayerState.AFK)
                .forEach(tabListService::refreshAfk);

        idleTracker.resetAutoTeleportAttempts();

        return true;
    }

    private boolean validateConfiguration(AFKAreaConfig candidate) {
        ConfigValidationResult result = AFKAreaConfigValidator.validate(candidate);

        for(String warning : result.warnings()) {
            getLogger().warning("Configuration warning: " + warning);
        }

        for(String error : result.errors()) {
            getLogger().severe("Configuration error: " + error);
        }

        return result.isValid();
    }

    public boolean persistConfig() {
        try {
            configFile.saveConfigObject(config);
            return true;
        }catch(RuntimeException exception) {
            getLogger().severe("Failed to persist config.yml: " + exception.getMessage());
            exception.printStackTrace();
            return false;
        }
    }

    private record DatabaseConfigSnapshot(String backend, boolean useQueue, long timeoutMillis, int poolSize, String tablePrefix, String mysqlUrl, String mysqlUser, String mysqlPassword) {
        static DatabaseConfigSnapshot from(AFKAreaConfig config) {
            return new DatabaseConfigSnapshot(config.databaseBackend, config.databaseUseQueue, config.databaseTimeoutMillis, config.databasePoolSize, config.databaseTablePrefix, config.databaseMysqlUrl, config.databaseMysqlUser, config.databaseMysqlPassword);
        }
    }

    public WorldGuardIntegration worldGuardIntegration() { return worldGuardIntegration; }
    public AFKAreaConfig config() { return config; }
    public MessageConfig messages() { return messages; }
    public MessageService messageService() { return messageService; }
    public PlayerStateService playerStateService() { return playerStateService; }
    public ActivityService activityService() { return activityService; }
    public TabListService tabListService() { return tabListService; }
    public Database<AreaData> areaDatabase() { return areaDatabase;  }
    public AreaManager areaManager() { return areaManager;  }
    public SelectionService selectionService() { return selectionService; }
    public AreaSessionService areaSessionService() { return areaSessionService; }
    public AreaTeleportService areaTeleportService() { return areaTeleportService; }
    public RewardService rewardService() { return rewardService; }
    public AFKPlayerStatsService playerStatsService() { return playerStatsService; }
    public HouseholdManager householdManager() { return householdManager; }
    public RewardSessionService rewardSessionService() { return rewardSessionService; }
    public BossBarDisplayService bossBarDisplayService() { return bossBarDisplayService; }
    public ActionBarDisplayService actionBarDisplayService() { return actionBarDisplayService; }
}
