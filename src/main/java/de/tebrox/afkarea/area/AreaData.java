package de.tebrox.afkarea.area;

import de.tebrox.afkarea.region.data.CuboidRegionData;
import de.tebrox.afkarea.region.data.WorldGuardRegionData;
import de.tebrox.afkarea.reward.data.RewardConfigData;
import de.tebrox.vertexCore.database.DataObject;
import de.tebrox.vertexCore.database.annotation.DbExpose;

public final class AreaData implements DataObject {
    private String uniqueId;

    @DbExpose private String name;
    @DbExpose private boolean enabled = true;
    @DbExpose private int priority = 0;
    @DbExpose private String regionType;
    @DbExpose private CuboidRegionData cuboidRegion;
    @DbExpose private WorldGuardRegionData worldGuardRegion;
    @DbExpose private TeleportData teleport;
    @DbExpose private RewardConfigData rewards;

    public AreaData() {}

    public AreaData(String uniqueId, String name) {
        this.uniqueId = uniqueId;
        this.name = name;
    }

    @Override
    public String getUniqueId() {
        return uniqueId;
    }

    @Override
    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getRegionType() {
        return regionType;
    }

    public void setRegionType(String regionType) {
        this.regionType = regionType;
    }

    public CuboidRegionData getCuboidRegion() {
        return cuboidRegion;
    }

    public void setCuboidRegion(CuboidRegionData cuboidRegion) {
        this.cuboidRegion = cuboidRegion;
    }

    public WorldGuardRegionData getWorldGuardRegion() { return worldGuardRegion; }

    public void setWorldGuardRegion(WorldGuardRegionData worldGuardRegion) { this.worldGuardRegion = worldGuardRegion; }

    public TeleportData getTeleport() {
        return teleport;
    }

    public void setTeleport(TeleportData teleport) {
        this.teleport = teleport;
    }

    public RewardConfigData getRewards() {
        return rewards;
    }

    public void setRewards(RewardConfigData rewards) {
        this.rewards = rewards;
    }

    public AreaData copy() {
        AreaData copy = new AreaData(uniqueId, name);

        copy.enabled = enabled;
        copy.priority = priority;
        copy.regionType = regionType;
        copy.cuboidRegion = cuboidRegion;
        copy.worldGuardRegion = worldGuardRegion;
        copy.teleport = teleport;
        copy.rewards = rewards == null ? null : rewards.copy();

        return copy;
    }
}
