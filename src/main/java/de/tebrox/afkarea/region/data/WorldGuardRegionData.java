package de.tebrox.afkarea.region.data;

public final class WorldGuardRegionData {
    private String world;
    private String regionId;
    private boolean includeChildren;

    public WorldGuardRegionData() {}

    public WorldGuardRegionData(String world, String regionId) {
        this(world, regionId, false);
    }

    public WorldGuardRegionData(String world, String regionId, boolean includeChildren) {
        this.world = world;
        this.regionId = regionId;
        this.includeChildren = includeChildren;
    }

    public String getWorld() {
        return world;
    }

    public String getRegionId() {
        return regionId;
    }

    public boolean isIncludeChildren() {
        return includeChildren;
    }
}
