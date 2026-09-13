package de.tebrox.afkarea.config;

import de.tebrox.vertexCore.config.ConfigObject;
import de.tebrox.vertexCore.config.annotation.ConfigKey;
import de.tebrox.vertexCore.config.annotation.ConfigSection;
import de.tebrox.vertexCore.config.annotation.StoreAt;

import java.beans.Visibility;

@StoreAt("config.yml")
public final class AFKAreaConfig implements ConfigObject {

    @ConfigKey("afk.mark-after-seconds")
    public int markAfterSeconds = 300;

    @ConfigKey("afk.teleport-after-seconds")
    public int teleportAfterSeconds = 1800;

    @ConfigKey("afk.auto-teleport.enabled")
    public boolean autoTeleportEnabled = true;

    @ConfigKey("afk.auto-teleport.target-area")
    public String autoTeleportTargetArea = "spawn-afk";

    @ConfigKey("visibility.hide-from-players")
    public boolean hideFromPlayers = true;

    @ConfigKey("visibility.hide-from-tablist")
    public boolean hideFromTablist = true;

    @ConfigKey("visibility.staff-view-permission")
    public String staffViewPermission = "afkarea.staff.see-hidden";

    @ConfigKey("anti-abuse.max-rewarding-players-per-ip")
    public int maxRewardingPlayersPerIp = 2;
}