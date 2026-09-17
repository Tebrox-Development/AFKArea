package de.tebrox.afkarea.activity;

import de.tebrox.afkarea.area.AreaEntrySource;
import de.tebrox.afkarea.area.AreaSessionService;
import de.tebrox.afkarea.area.AreaTeleportService;
import de.tebrox.afkarea.config.AFKAreaConfig;
import de.tebrox.afkarea.config.MessageConfig;
import de.tebrox.afkarea.display.TabListService;
import de.tebrox.afkarea.message.MessageService;
import de.tebrox.afkarea.state.PlayerState;
import de.tebrox.afkarea.state.PlayerStateService;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

public final class IdleTracker {
    private final JavaPlugin plugin;
    private final ActivityService activityService;
    private final PlayerStateService stateService;
    private final Supplier<AFKAreaConfig> config;
    private final Supplier<MessageConfig> messages;
    private final MessageService messageService;
    private final TabListService tabListService;
    private final AreaTeleportService areaTeleportService;
    private final AreaSessionService sessionService;

    private final Set<UUID> autoTeleportAttempts = new HashSet<>();

    private BukkitTask task;

    public IdleTracker(
            JavaPlugin plugin,
            ActivityService activityService,
            PlayerStateService stateService,
            Supplier<AFKAreaConfig> config,
            Supplier<MessageConfig> messages,
            MessageService messageService,
            TabListService tabListService, AreaTeleportService areaTeleportService, AreaSessionService sessionService
    ) {
        this.plugin = plugin;
        this.activityService = activityService;
        this.stateService = stateService;
        this.config = config;
        this.messages = messages;
        this.messageService = messageService;
        this.tabListService = tabListService;
        this.areaTeleportService = areaTeleportService;
        this.sessionService = sessionService;
    }

    public void start() {
        if(task != null) return;

        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L);
    }

    public void stop() {
        if(task == null) return;

        task.cancel();
        task = null;
    }

    private void tick() {
        AFKAreaConfig current = config.get();
        Duration afkTimeout = Duration.ofSeconds(current.markAfterSeconds);
        Duration teleportTimeout = Duration.ofSeconds(current.teleportAfterSeconds);

        for(Player player : plugin.getServer().getOnlinePlayers()) {
            UUID playerId = player.getUniqueId();
            PlayerState state = stateService.getState(playerId);

            if(state == PlayerState.AFK_AREA) {
                autoTeleportAttempts.remove(playerId);
                continue;
            }

            if(isAutomationSuspended(player)) {
                activityService.recordActivity(playerId);
                autoTeleportAttempts.remove(playerId);
                continue;
            }

            Duration idle = activityService.getIdleDuration(playerId);

            if(idle.compareTo(teleportTimeout) < 0) {
                autoTeleportAttempts.remove(playerId);
            }

            if(state == PlayerState.ACTIVE && idle.compareTo(afkTimeout) >= 0) {
                stateService.setState(playerId, PlayerState.AFK);
                tabListService.applyAfk(player);
                messageService.send(player, messages.get().afkEnabled);
                state = PlayerState.AFK;
            }


            if(!current.autoTeleportEnabled) {
                autoTeleportAttempts.remove(playerId);
                continue;
            }

            if(state != PlayerState.AFK) continue;
            if(idle.compareTo(teleportTimeout) < 0) continue;

            attemptAutoTeleport(player, current);
        }
    }

    private void attemptAutoTeleport(Player player, AFKAreaConfig current) {
        UUID playerId = player.getUniqueId();

        if(!autoTeleportAttempts.add(playerId)) return;

        String areaId = current.autoTeleportTargetArea;
        if(areaId == null || areaId.isBlank()) {
            plugin.getLogger().warning("Automatic AFK teleport for player '" + player.getName() + "' failed: no target area is configured");
            return;
        }
        sessionService.markNextEntry(playerId, AreaEntrySource.AUTOMATIC);
        AreaTeleportService.Result result = areaTeleportService.teleport(player, areaId, true);

        if(result != AreaTeleportService.Result.SUCCESS) {
            plugin.getLogger().warning("Automatic AFK teleport for player '" + player.getName() + "' to area '" + areaId + "' failed: " + result);
            sessionService.clearPendingEntry(playerId);
        }
    }

    private boolean isAutomationSuspended(Player player) {
        return player.hasPermission("afkarea.bypass.auto-afk")
                || player.getGameMode() == GameMode.SPECTATOR
                || player.isDead()
                || player.isSleeping();
    }

    public void resetAutoTeleportAttempts() {
        autoTeleportAttempts.clear();
    }
}
