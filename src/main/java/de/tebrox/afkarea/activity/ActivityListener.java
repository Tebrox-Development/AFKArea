package de.tebrox.afkarea.activity;

import de.tebrox.afkarea.config.MessageConfig;
import de.tebrox.afkarea.display.TabListService;
import de.tebrox.afkarea.message.MessageService;
import de.tebrox.afkarea.state.PlayerState;
import de.tebrox.afkarea.state.PlayerStateService;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;
import java.util.function.Supplier;

public final class ActivityListener implements Listener {
    private final JavaPlugin plugin;
    private final ActivityService activityService;
    private final PlayerStateService stateService;
    private final Supplier<MessageConfig> messages;
    private final MessageService messageService;
    private final TabListService tabListService;

    public ActivityListener(
            JavaPlugin plugin,
            ActivityService activityService,
            PlayerStateService stateService,
            Supplier<MessageConfig> messages,
            MessageService messageService,
            TabListService tabListService
    ) {
        this.plugin = plugin;
        this.activityService = activityService;
        this.stateService = stateService;
        this.messages = messages;
        this.messageService = messageService;
        this.tabListService = tabListService;
    }

    private void record(Player player) {
        record(player, true);
    }

    private void record(Player player, boolean resetAfk) {
        UUID playerId = player.getUniqueId();
        activityService.recordActivity(playerId);

        if(!resetAfk) return;

        if(Bukkit.isPrimaryThread()) {
            resetAfk(playerId);
        }else{
            plugin.getServer().getScheduler().runTask(plugin, () -> resetAfk(playerId));
        }
    }

    private void resetAfk(UUID playerId) {
        if(stateService.getState(playerId) != PlayerState.AFK) return;

        Player player = plugin.getServer().getPlayer(playerId);
        if(player == null) return;

        stateService.setState(playerId, PlayerState.ACTIVE);
        tabListService.clearAfk(player);

        messageService.send(player, messages.get().afkDisabled);
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
        tabListService.forget(playerId);
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
        String message = event.getMessage();
        String command = message.substring(1).split("\\s+", 2)[0];

        boolean resetAfk = !command.equalsIgnoreCase("afk");

        record(event.getPlayer(), resetAfk);
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
