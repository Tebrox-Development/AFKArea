package de.tebrox.afkarea.region;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import de.tebrox.afkarea.integration.WorldGuardIntegration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.awt.*;

public final class WorldGuardRegionProvider implements RegionProvider {
    private final WorldGuardIntegration integration;
    private final String worldName;
    private final String regionId;
    private final boolean includeChildren;

    public WorldGuardRegionProvider(WorldGuardIntegration integration, String worldName, String regionId) {
        this(integration, worldName, regionId, false);
    }

    public WorldGuardRegionProvider(WorldGuardIntegration integration, String worldName, String regionId, boolean includeChildren) {
        this.integration = integration;
        this.worldName = worldName;
        this.regionId = regionId;
        this.includeChildren = includeChildren;
    }

    @Override
    public boolean contains(Location location) {
        if(location == null || location.getWorld() == null) return false;
        if(!integration.isAvailable()) return false;
        if(!worldName.equalsIgnoreCase(location.getWorld().getName())) return false;

        RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(location.getWorld()));
        if(regionManager == null) return false;

        ProtectedRegion rootRegion = regionManager.getRegion(regionId);
        if(rootRegion == null) return false;

        if(!includeChildren) return rootRegion.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ());

        BlockVector3 position = BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        ApplicableRegionSet regions = regionManager.getApplicableRegions(position, RegionQuery.QueryOption.COMPUTE_PARENTS);

        for(ProtectedRegion region : regions) {
            if(region.getId().equalsIgnoreCase(rootRegion.getId())) return true;
        }

        return false;
    }

    public boolean exists() {
        if(!integration.isAvailable()) return false;
        if(worldName == null || worldName.isBlank() || regionId == null || regionId.isBlank()) return false;

        World world = Bukkit.getWorld(worldName);
        if(world == null) return false;

        RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
        return regionManager != null && regionManager.hasRegion(regionId);
    }

    public String worldName() {
        return worldName;
    }

    public String regionId() {
        return regionId;
    }
}
