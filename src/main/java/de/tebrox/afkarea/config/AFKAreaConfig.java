package de.tebrox.afkarea.config;

import de.tebrox.afkarea.bootstrap.AFKAreaPermissions;
import de.tebrox.vertexCore.config.ConfigObject;
import de.tebrox.vertexCore.config.annotation.ConfigKey;
import de.tebrox.vertexCore.config.annotation.StoreAt;

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
    public String staffViewPermission = AFKAreaPermissions.STAFF_SEE_HIDDEN;

    @ConfigKey("anti-abuse.max-rewarding-players-per-ip")
    public int maxRewardingPlayersPerIp = 2;

    @ConfigKey("database.backend")
    public String databaseBackend = "json";

    @ConfigKey("database.use-queue")
    public boolean databaseUseQueue = true;

    @ConfigKey("database.timeout-millis")
    public long databaseTimeoutMillis = 5000;

    @ConfigKey("database.pool-size")
    public int databasePoolSize = 5;

    @ConfigKey("database.table-prefix")
    public String databaseTablePrefix = "afkarea_";

    @ConfigKey("database.mysql.url")
    public String databaseMysqlUrl = "";

    @ConfigKey("database.mysql.user")
    public String databaseMysqlUser = "";

    @ConfigKey("database.mysql.password")
    public String databaseMysqlPassword = "";

    @ConfigKey("display.bossbar.enabled")
    public boolean bossBarEnabled = true;

    @ConfigKey("display.bossbar.color")
    public String bossBarColor = "yellow";

    @ConfigKey("display.bossbar.style")
    public String bossBarStyle = "progress";
}