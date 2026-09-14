package de.tebrox.afkarea.activity;

import de.tebrox.afkarea.state.PlayerStateService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInputEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public final class ActivityListener implements Listener {
    private final ActivityService activityService;
    private final PlayerStateService stateService;

    public ActivityListener(ActivityService activityService, PlayerStateService stateService) {
        this.activityService = activityService;
        this.stateService = stateService;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        activityService.track(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();

        activityService.clear(playerId);
        stateService.clear(playerId);
    }

    @EventHandler
    public void onInput(PlayerInputEvent event) {
        activityService.recordActivity(event.getPlayer().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true)
    public void onLook(PlayerMoveEvent event) {
        if(event.getTo() == null) return;

        if(Float.compare(event.getFrom().getYaw(), event.getTo().getYaw()) != 0 || Float.compare(event.getFrom().getPitch(), event.getTo().getPitch()) != 0) {
            activityService.recordActivity(event.getPlayer().getUniqueId());
        }
    }
}
