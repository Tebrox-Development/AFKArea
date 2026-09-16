package de.tebrox.afkarea.bootstrap;

import de.tebrox.afkarea.activity.ActivityListener;
import de.tebrox.afkarea.activity.ActivityService;
import de.tebrox.afkarea.activity.IdleTracker;
import de.tebrox.afkarea.area.*;
import de.tebrox.afkarea.area.selection.SelectionService;
import de.tebrox.afkarea.command.AFKAreaCommands;
import de.tebrox.afkarea.command.AfkCommand;
import de.tebrox.afkarea.config.AFKAreaConfig;
import de.tebrox.afkarea.config.MessageConfig;
import de.tebrox.afkarea.display.AreaVisibilityListener;
import de.tebrox.afkarea.display.AreaVisibilityService;
import de.tebrox.afkarea.display.TabListService;
import de.tebrox.afkarea.household.HouseholdData;
import de.tebrox.afkarea.household.HouseholdManager;
import de.tebrox.afkarea.integration.WorldGuardIntegration;
import de.tebrox.afkarea.message.MessageService;
import de.tebrox.afkarea.persistence.AFKAreaDatabaseSettings;
import de.tebrox.afkarea.reward.RewardService;
import de.tebrox.afkarea.reward.RewardSessionService;
import de.tebrox.afkarea.state.PlayerState;
import de.tebrox.afkarea.state.PlayerStateService;
import de.tebrox.vertexCore.VertexCoreApi;
import de.tebrox.vertexCore.config.Config;
import de.tebrox.vertexCore.database.Database;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;


public final class AFKAreaPlugin extends JavaPlugin {
    private static final int BSTATS_PLUGIN_ID = 34037;

    private Config<AFKAreaConfig> configFile;
    private AFKAreaConfig config;

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

    private WorldGuardIntegration worldGuardIntegration;

    @Override
    public void onEnable() {
        worldGuardIntegration = WorldGuardIntegration.detect(this);

        if(worldGuardIntegration.isAvailable()) {
            getLogger().info("WorldGuard integration is available");
        }else{
            getLogger().info("WorldGuard not found - Worldguard integration is disabled");
        }

        configFile = new Config<>(this, AFKAreaConfig.class);
        config = configFile.loadConfigObject();

        AFKAreaDatabaseSettings databaseSettings = new AFKAreaDatabaseSettings(config);
        areaDatabase = new Database<>(this, databaseSettings, AreaData.class);
        householdDatabase = new Database<>(this, databaseSettings, HouseholdData.class);
        areaManager = new AreaManager(this, areaDatabase, worldGuardIntegration);
        householdManager = new HouseholdManager(this, householdDatabase);
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


        areaSessionService = new AreaSessionService(areaManager, playerStateService, activityService, tabListService, () -> messages, messageService, visibilityService, rewardSessionService);
        areaManager.setRuntimeChangeListener(() -> getServer().getOnlinePlayers().forEach(areaSessionService::sync));
        getServer().getPluginManager().registerEvents(new AreaSessionListener(areaSessionService), this);
        getServer().getPluginManager().registerEvents(new AreaVisibilityListener(this, visibilityService), this);
        areaManager.loadAsync();
        householdManager.loadAsync();

        selectionService = new SelectionService();
        rewardSessionService.start();

        idleTracker = new IdleTracker(this, activityService, playerStateService, () -> config, () -> messages, messageService, tabListService, areaTeleportService);
        idleTracker.start();

        getServer().getPluginManager().registerEvents(new ActivityListener(this, activityService, playerStateService, () -> messages, messageService, tabListService), this);

        getServer().getOnlinePlayers().forEach(player -> activityService.track(player.getUniqueId())
        );

        VertexCoreApi.get().commands().register(this, new AFKAreaCommands(this));
        VertexCoreApi.get().commands().register(this, new AfkCommand(this));

        new Metrics(this, BSTATS_PLUGIN_ID);
        getLogger().info("AFKArea has been enabled");
    }

    @Override
    public void onDisable() {
        idleTracker.stop();

        VertexCoreApi.get().commands().unregisterAll(this);

        visibilityService.restoreAll(getServer().getOnlinePlayers());
        tabListService.restoreAll(getServer().getOnlinePlayers());
        selectionService.clearAll();

        playerStateService.clearAll();
        activityService.clearAll();

        if(householdManager != null) householdManager.clear();
        if(areaManager != null) areaManager.clear();
        if(areaDatabase != null) areaDatabase.close();

        areaSessionService.clearAll();
        rewardSessionService.stop();

        getLogger().info("AFKArea has been disabled");
    }

    public void reloadConfigs() {
        config = configFile.loadConfigObject();
        messages = messageFile.loadConfigObject();

        visibilityService.refreshAll(getServer().getOnlinePlayers());

        areaManager.loadAsync();

        getServer().getOnlinePlayers().stream()
                .filter(player -> playerStateService.getState(player.getUniqueId()) == PlayerState.AFK)
                .forEach(tabListService::refreshAfk);

        idleTracker.resetAutoTeleportAttempts();
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
    public HouseholdManager householdManager() { return householdManager; }
}
