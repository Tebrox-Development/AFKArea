package de.tebrox.afkarea.config;

import de.tebrox.vertexCore.config.ConfigObject;
import de.tebrox.vertexCore.config.annotation.ConfigKey;
import de.tebrox.vertexCore.config.annotation.StoreAt;

@StoreAt("messages.yml")
public final class MessageConfig implements ConfigObject {

    @ConfigKey("prefix")
    public String prefix = "<gray>[<gold>AFKArea</gold>]</gray> ";

    @ConfigKey("afk.enabled")
    public String afkEnabled = "<yellow>You are now AFK.</yellow>";

    @ConfigKey("afk.disabled")
    public String afkDisabled = "<green>You are no longer AFK.</green>";

    @ConfigKey("errors.no-permission")
    public String noPermission = "<red>You do not have permission to do that.</red>";

    @ConfigKey("errors.player-only")
    public String playerOnly = "<red>This command can only be used by players.</red>";

    @ConfigKey("errors.unknown-area")
    public String unknownArea = "<red>AFK area '<area>' does not exist.</red>";

    @ConfigKey("reload.success")
    public String reloadSuccess = "<green>AFKArea configuration reloaded.</green>";
}