package de.tebrox.afkarea.activity;

import de.tebrox.afkarea.config.AFKAreaConfig;
import de.tebrox.afkarea.config.MessageConfig;
import de.tebrox.afkarea.message.MessageService;
import de.tebrox.afkarea.state.PlayerState;
import de.tebrox.afkarea.state.PlayerStateService;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.UUID;
import java.util.function.Supplier;

public final class IdleTracker {
    private final JavaPlugin plugin;
    private final ActivityService activityService;
    private final PlayerStateService stateService;
    private final Supplier<AFKAreaConfig> config;
    private final Supplier<MessageConfig> messages;
    private final MessageService messageService;

    private BukkitTask task;

    public IdleTracker(
            JavaPlugin plugin,
            ActivityService activityService,
            PlayerStateService stateService,
            Supplier<AFKAreaConfig> config,
            Supplier<MessageConfig> messages,
            MessageService messageService
    ) {
        this.plugin = plugin;
        this.activityService = activityService;
        this.stateService = stateService;
        this.config = config;
        this.messages = messages;
        this.messageService = messageService;
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
        Duration afkTimout = Duration.ofSeconds(config.get().markAfterSeconds);

        for(Player player : plugin.getServer().getOnlinePlayers()) {
            UUID playerId = player.getUniqueId();

            if(player.hasPermission("afkarea.bypass.auto-afk")) continue;
            if(stateService.getState(playerId) != PlayerState.ACTIVE) continue;
            if(activityService.getIdleDuration(playerId).compareTo(afkTimout) < 0) continue;

            stateService.setState(playerId, PlayerState.AFK);

            messageService.send(player, messages.get().afkEnabled);
        }
    }
}
