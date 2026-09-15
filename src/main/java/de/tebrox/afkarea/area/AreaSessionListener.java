package de.tebrox.afkarea.area;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public final class AreaSessionListener implements Listener {
    private final AreaSessionService sessionService;

    public AreaSessionListener(AreaSessionService sessionService) {
        this.sessionService = sessionService;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Location to = event.getTo();

        if(to == null) return;
        if(!changedBlock(event.getFrom(), to)) return;

        sessionService.sync(event.getPlayer(), to);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        Location to = event.getTo();

        if(to == null) return;
        sessionService.sync(event.getPlayer(), to);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        sessionService.sync(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        sessionService.clear(event.getPlayer().getUniqueId());
    }

    private boolean changedBlock(Location from, Location to) {
        return from.getWorld() != to.getWorld()
                || from.getBlockX() != to.getBlockX()
                || from.getBlockY() != to.getBlockY()
                || from.getBlockZ() != to.getBlockZ();
    }
}
