package de.tebrox.afkarea.activity;

import de.tebrox.afkarea.state.PlayerStateService;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;

import java.util.UUID;

public final class ActivityListener implements Listener {
    private final ActivityService activityService;
    private final PlayerStateService stateService;

    public ActivityListener(ActivityService activityService, PlayerStateService stateService) {
        this.activityService = activityService;
        this.stateService = stateService;
    }

    private void record(Player player) {
        activityService.recordActivity(player.getUniqueId());
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
        record(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onLook(PlayerMoveEvent event) {
        if(event.getTo() == null) return;

        if(Float.compare(event.getFrom().getYaw(), event.getTo().getYaw()) != 0 || Float.compare(event.getFrom().getPitch(), event.getTo().getPitch()) != 0) {
            record(event.getPlayer());
        }
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        record(event.getPlayer());
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        record(event.getPlayer());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if(event.getWhoClicked() instanceof Player player) {
            record(player);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if(event.getWhoClicked() instanceof Player player) {
            record(player);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        record(event.getPlayer());
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        record(event.getPlayer());
    }

    @EventHandler
    public void onHeldItemChange(PlayerItemHeldEvent event) {
        record(event.getPlayer());
    }

    @EventHandler
    public void onSwapHands(PlayerSwapHandItemsEvent event) {
        record(event.getPlayer());
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        record(event.getPlayer());
    }
}
