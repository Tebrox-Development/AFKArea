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

    @ConfigKey("tab.afk-format")
    public String afkTabFormat = "<player> <gray>[<yellow>AFK</yellow>]</gray>";

    @ConfigKey("errors.no-permission")
    public String noPermission = "<red>You do not have permission to do that.</red>";

    @ConfigKey("errors.player-only")
    public String playerOnly = "<red>This command can only be used by players.</red>";

    @ConfigKey("errors.unknown-area")
    public String unknownArea = "<red>AFK area '<area>' does not exist.</red>";

    @ConfigKey("reload.success")
    public String reloadSuccess = "<green>AFKArea configuration reloaded.</green>";

    @ConfigKey("selection.pos1-set")
    public String selectionPos1Set = "<green>Position 1 set to <gray><world> <x> <y> <z></gray>.</green>";

    @ConfigKey("selection.pos2-set")
    public String selectionPos2Set = "<green>Position 2 set to <gray><world> <x> <y> <z></gray>.</green>";

    @ConfigKey("area.create-usage")
    public String areaCreateUsage = "<red>Usage: /afkarea create <id> cuboid</red>";

    @ConfigKey("area.invalid-id")
    public String areaInvalidId = "<red>Area IDs may only contain lowercase letters, numbers, '-' and '_'.</red>";

    @ConfigKey("area.already-exists")
    public String areaAlreadyExists = "<red>AFK area '<area>' already exists.</red>";

    @ConfigKey("area.selection-incomplete")
    public String areaSelectionIncomplete = "<red>You must set both selection positions first.</red>";

    @ConfigKey("area.selection-world-mismatch")
    public String areaSelectionWorldMismatch = "<red>Both selection positions must be in the same world.</red>";

    @ConfigKey("area.unsupported-region-type")
    public String areaUnsupportedRegionType = "<red>Unsupported region type '<type>'.</red>";

    @ConfigKey("area.created")
    public String areaCreated = "<green>AFK area '<area>' was created successfully.</green>";

    @ConfigKey("area.save-failed")
    public String areaSaveFailed = "<red>Failed to save AFK area '<area>'. Check the server console.</red>";

    @ConfigKey("area.redefine-usage")
    public String areaRedefineUsage = "<red>Usage: /afkarea redefine <id></red>";

    @ConfigKey("area.not-cuboid")
    public String areaNotCuboid = "<red>AFK area '<area>' is not a cuboid area.</red>";

    @ConfigKey("area.redefined")
    public String areaRedefined = "<green>AFK area '<area>' was redefined successfully.</green>";

    @ConfigKey("area.rename-usage")
    public String areaRenameUsage = "<red>Usage: /afkarea rename <id> <name></red>";

    @ConfigKey("area.invalid-name")
    public String areaInvalidName = "<red>The area name must not be empty.</red>";

    @ConfigKey("area.renamed")
    public String areaRenamed = "<green>AFK area '<area>' was renamed to '<name>'.</green>";

    @ConfigKey("area.delete-usage")
    public String areaDeleteUsage = "<red>Usage: /afkarea delete <id></red>";

    @ConfigKey("area.deleted")
    public String areaDeleted = "<green>AFK area '<area>' was deleted successfully.</green>";

    @ConfigKey("area.delete-failed")
    public String areaDeleteFailed = "<red>Failed to delete AFK area '<area>'. Check the server console.</red>";

    @ConfigKey("area.setteleport-usage")
    public String areaSetTeleportUsage = "<red>Usage: /afkarea setteleport <id></red>";

    @ConfigKey("area.teleport-set")
    public String areaTeleportSet = "<green>Teleport location for AFK area '<area>' was updated.</green>";

    @ConfigKey("area.tp-usage")
    public String areaTpUsage = "<red>Usage: /afkarea tp <id></red>";

    @ConfigKey("area.teleport-not-set")
    public String areaTeleportNotSet = "<red>AFK area '<area>' has no teleport location configured.</red>";

    @ConfigKey("area.teleport-world-unavailable")
    public String areaTeleportWorldUnavailable = "<red>The world '<world>' for AFK area '<area>' is not available.</red>";

    @ConfigKey("area.teleported")
    public String areaTeleported = "<green>Teleported to AFK area '<area>'.</green>";

    @ConfigKey("area.list-empty")
    public String areaListEmpty = "<yellow>No AFK areas are configured.</yellow>";

    @ConfigKey("area.list")
    public String areaList = "<gold>AFK areas (<count>):</gold>\n<gray><areas></gray>";

    @ConfigKey("area.info-usage")
    public String areaInfoUsage = "<red>Usage: /afkarea info <id></red>";

    @ConfigKey("area.info")
    public String areaInfo = "<gold>AFK area '<area>'</gold>\n"
                    + "<gray>Name: <white><name></white>\n"
                    + "Enabled: <white><enabled></white>\n"
                    + "Priority: <white><priority></white>\n"
                    + "Region: <white><region></white>\n"
                    + "Teleport: <white><teleport></white></gray>";

    @ConfigKey("area.entered")
    public String areaEntered = "<yellow>You entered AFK area '<area>'.</yellow>";

    @ConfigKey("area.left")
    public String areaLeft = "<green>You left AFK area '<area>'.</green>";

    @ConfigKey("tab.afk-area-format")
    public String afkAreaTabFormat = "<player> <gray>[<aqua>AFK-ZONE</aqua>]</gray>";
}