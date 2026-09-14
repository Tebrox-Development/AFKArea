package de.tebrox.afkarea.region;

import de.tebrox.afkarea.region.data.CuboidRegionData;
import org.bukkit.Location;
import org.bukkit.World;


public class CuboidRegionProvider implements RegionProvider {
    private final CuboidRegionData data;

    public  CuboidRegionProvider(CuboidRegionData data) {
        this.data = data;
    }

    @Override
    public boolean contains(Location location) {
        if(location == null) return false;

        World world = location.getWorld();

        if(data.getWorld() == null || !data.getWorld().equals(world.getName())) return false;

        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        return x >= data.getMinX() && x <= data.getMaxX() && y >= data.getMinY() && y <= data.getMaxY() && z >= data.getMinZ() && z <= data.getMaxZ();
    }
}
