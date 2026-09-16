package de.tebrox.afkarea.region;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
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
        if(location == null || location.getWorld() == null) return false;
        if(!integration.isAvailable()) return false;
        if(!worldName.equalsIgnoreCase(location.getWorld().getName())) return false;

        RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(location.getWorld()));
        if(regionManager == null) return false;

        ProtectedRegion region = regionManager.getRegion(regionId);
        if(region == null) return false;

        return region.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public String worldName() {
        return worldName;
    }

    public String regionId() {
        return regionId;
    }
}
