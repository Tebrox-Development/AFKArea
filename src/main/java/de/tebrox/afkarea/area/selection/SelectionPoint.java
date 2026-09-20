package de.tebrox.afkarea.area.selection;

import org.bukkit.Location;

public record SelectionPoint(String world, int x, int y, int z) {
    public static SelectionPoint from(Location location) {
        return new SelectionPoint(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }
}
