package de.tebrox.afkarea.region;

import de.tebrox.afkarea.integration.WorldGuardIntegration;
import org.bukkit.Location;

public final class WorldGuardRegionProvider implements RegionProvider {
    private final WorldGuardIntegration integration;
    private final String worldName;
    private final String regionId;

    public WorldGuardRegionProvider(WorldGuardIntegration integration, String worldName, String regionId) {
        this.integration = integration;
        this.worldName = worldName;
        this.regionId = regionId;
    }

    @Override
    public boolean contains(Location location) {
        return false;
    }

    public String worldName() {
        return worldName;
    }

    public String regionId() {
        return regionId;
    }
}
