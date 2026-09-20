package de.tebrox.afkarea.region.data;

public final class CuboidRegionData {
     private String world;
    private int minX, minY, minZ;
    private int maxX, maxY, maxZ;

    public CuboidRegionData() {}

    public CuboidRegionData(
            String world,
            int x1,
            int y1,
            int z1,
            int x2,
            int y2,
            int z2) {
        this.world = world;

        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.minZ = Math.min(z1, z2);

        this.maxX = Math.max(x1, x2);
        this.maxY = Math.max(y1, y2);
        this.maxZ = Math.max(z1, z2);
    }

    public String getWorld() {
        return world;
    }

    public int getMinX() {
        return minX;
    }

    public int getMinY() {
        return minY;
    }

    public int getMinZ() {
        return minZ;
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMaxY() {
        return maxY;
    }

    public int getMaxZ() {
        return maxZ;
    }
}
