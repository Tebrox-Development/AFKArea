package de.tebrox.afkarea.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class AFKAreaConfigValidator {
    private static final Set<String> SUPPORTED_DATABASE_BACKENDS = Set.of("json", "h2", "mysql");
    private static final Set<String> SUPPORTED_BOSSBAR_COLORS = Set.of("pink", "blue", "red", "green", "yellow", "purple", "white");
    private static final Set<String> SUPPORTED_BOSSBAR_STYLES = Set.of("progress", "notched-6", "notched-10", "notched-12", "notched-20");

    private AFKAreaConfigValidator() {}

    public static ConfigValidationResult validate(AFKAreaConfig config) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        if(config == null) {
            errors.add("Configuration could not be loaded.");
            return new ConfigValidationResult(errors, warnings);
        }

        if(config.markAfterSeconds < 0) errors.add("afk.mark-after-seconds must be greater than or equal to 0.");
        if(config.teleportAfterSeconds <= 0) errors.add("afk.teleport-after-seconds must be greater than 0.");
        if(config.teleportAfterSeconds < config.markAfterSeconds) errors.add("afk.teleport-after-seconds must not be lower than afk.mark-after-seconds.");
        if(config.autoTeleportEnabled && (config.autoTeleportTargetArea == null || config.autoTeleportTargetArea.isBlank())) errors.add("afk.auto-teleport.target-area must not be blank while automatic teleporting is enabled");
        if(config.staffViewPermission == null || config.staffViewPermission.isBlank()) warnings.add("visibility.staff-view-permission is blank. Staff will not receive the AFK-area visibility bypass.");
        if(config.maxRewardingPlayersPerIp < -1 || config.maxRewardingPlayersPerIp == 0) errors.add("anti-abuse.max-rewarding-players-per-ip must be -1 for unlimited or at least 1.");

        String backend = config.databaseBackend == null ? "" : config.databaseBackend.trim().toLowerCase(Locale.ROOT);
        if(!SUPPORTED_DATABASE_BACKENDS.contains(backend)) errors.add("database.backend '" + config.databaseBackend + "' is unsupported. Supported backends: json, h2, mysql.");

        if(config.databaseTimeoutMillis <= 0) errors.add("database.timeout-millis must be greater than 0.");
        if(config.databasePoolSize <= 0) errors.add("database.pool-size must be greater than 0.");
        if(config.databaseTablePrefix == null || config.databaseTablePrefix.isBlank()) warnings.add("database.table-prefix is blank.");
        else if(!config.databaseTablePrefix.matches("[A-Za-z0-9_]+")) errors.add("database.table-prefix may only contain letters, numbers and underscores");

        if("mysql".equals(backend)) {
            if(config.databaseMysqlUrl == null || config.databaseMysqlUrl.isBlank()) errors.add("database.mysql.url must not be blank when database.backend is mysql");
            if(config.databaseMysqlUser == null || config.databaseMysqlUser.isBlank()) errors.add("database.mysql.user must not be blank when database.backend is mysql");
        }

        String bossBarColor = config.bossBarColor == null ? "" : config.bossBarColor.trim().toLowerCase(Locale.ROOT);
        if(!SUPPORTED_BOSSBAR_COLORS.contains(bossBarColor)) errors.add("display.bossbar.color '" + config.bossBarColor + "' is unsupported. Supported colors: " + String.join(", ", SUPPORTED_BOSSBAR_COLORS) + ".");

        String bossBarStyle = config.bossBarStyle == null ? "" : config.bossBarStyle.trim().toLowerCase(Locale.ROOT);
        if(!SUPPORTED_BOSSBAR_STYLES.contains(bossBarStyle)) errors.add("display.bossbar.style '" + config.bossBarStyle + "' is unsupported. Supported styles: " + String.join(", ", SUPPORTED_BOSSBAR_STYLES) + ".");

        return new ConfigValidationResult(errors, warnings);
    }
}
