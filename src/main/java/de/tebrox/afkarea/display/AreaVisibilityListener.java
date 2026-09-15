package de.tebrox.afkarea.display;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class AreaVisibilityListener implements Listener {
    private final JavaPlugin plugin;
    private final AreaVisibilityService visibilityService;

    public AreaVisibilityListener(JavaPlugin plugin, AreaVisibilityService visibilityService) {
        this.plugin = plugin;
        this.visibilityService = visibilityService;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if(event.getPlayer().isOnline()) {
                visibilityService.syncViewer(event.getPlayer());
            }
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        visibilityService.forget(event.getPlayer().getUniqueId());
    }
}
