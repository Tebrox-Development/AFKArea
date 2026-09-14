package de.tebrox.afkarea.region;

import org.bukkit.Location;

public interface RegionProvider {
    boolean contains(Location location);
}
