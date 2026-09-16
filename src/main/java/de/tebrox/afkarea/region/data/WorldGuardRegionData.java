package de.tebrox.afkarea.region.data;

public final class WorldGuardRegionData {
    private String world;
    private String regionId;

    public WorldGuardRegionData() {}

    public WorldGuardRegionData(String world, String regionId) {
        this.world = world;
        this.regionId = regionId;
    }

    public String getWorld() {
        return world;
    }

    public String getRegionId() {
        return regionId;
    }
}
