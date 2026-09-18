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
    public String areaCreateUsage = "<red>Usage: /afkarea create <id> <cuboid|worldguard></red>";

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
    public String areaLeft = "<green>You left AFK area '<area>' after <yellow><duration></yellow>.</green>";

    @ConfigKey("tab.afk-area-format")
    public String afkAreaTabFormat = "<player> <gray>[<aqua>AFK-ZONE</aqua>]</gray>";

    @ConfigKey("area.unavailable")
    public String areaUnavailable = "<red>The configured AFK area is currently unavailable.</red>";

    @ConfigKey("reward.received")
    public String rewardReceived = "<green>You received reward <yellow><reward></yellow> in <aqua><area_name></aqua>.</green>";

    @ConfigKey("household.link-usage")
    public String householdLinkUsage = "<red>Usage: /afkarea household link <player1> <player2></red>";

    @ConfigKey("household.unlink-usage")
    public String householdUnlinkUsage = "<red>Usage: /afkarea household unlink <player></red>";

    @ConfigKey("household.unknown-player")
    public String householdUnknownPlayer = "<red>Player '<player>' is not known to this server.</red>";

    @ConfigKey("household.same-player")
    public String householdSamePlayer = "<red>A player cannot be linked to themselves.</red>";

    @ConfigKey("household.already-same")
    public String householdAlreadySame = "<yellow><player1> and <player2> are already part of the same household.</yellow>";

    @ConfigKey("household.different-households")
    public String householdDifferentHouseholds = "<red><player1> and <player2> already belong to different households. Unlink one of them first.</red>";

    @ConfigKey("household.linked")
    public String householdLinked = "<green>Linked <player1> and <player2> as household members.</green>";

    @ConfigKey("household.not-linked")
    public String householdNotLinked = "<yellow><player> is not part of a household.</yellow>";

    @ConfigKey("household.unlinked")
    public String householdUnlinked = "<green>Removed <player> from their household.</green>";

    @ConfigKey("household.data-loading")
    public String householdDataLoading = "<yellow>Household data is still loading. Please try again.</yellow>";

    @ConfigKey("household.busy")
    public String householdBusy = "<yellow>This household is currently being updated. Please try again.</yellow>";

    @ConfigKey("household.save-failed")
    public String householdSaveFailed = "<red>Failed to update household data. Check the server console.</red>";

    @ConfigKey("household.info-usage")
    public String householdInfoUsage = "<red>Usage: /afkarea household info <player></red>";

    @ConfigKey("household.info")
    public String householdInfo = "<gray>Household of <white><player></white>: <aqua><members></aqua></gray>";

    @ConfigKey("household.list-empty")
    public String householdListEmpty = "<yellow>No households are configured.</yellow>";

    @ConfigKey("household.list")
    public String householdList = "<gray>Households (<white><count></white>):\n<households></gray>";

    @ConfigKey("area.create-worldguard-usage")
    public String areaCreateWorldGuardUsage = "<red>Usage: /afkarea create <id> worldguard <region></red>";

    @ConfigKey("integrations.worldguard-unavailable")
    public String worldGuardUnavailable = "<red>WorldGuard is not available on this server.</red>";

    @ConfigKey("area.setregion-usage")
    public String areaSetRegionUsage = "<red>Usage: /afkarea setregion <id> <region></red>";

    @ConfigKey("area.not-worldguard")
    public String areaNotWorldGuard = "<red>AFK area '<area>' is not a WorldGuard area.</red>";

    @ConfigKey("area.region-set")
    public String areaRegionSet = "<green>WorldGuard region for AFK area '<area>' was set to '<region>' in world '<world>'.</green>";

    @ConfigKey("integrations.worldguard-region-not-found")
    public String worldGuardRegionNotFound = "<red>WorldGuard region '<region>' does not exist in world '<world>'.</red>";

    @ConfigKey("area.setpriority-usage")
    public String areaSetPriorityUsage = "<red>Usage: /afkarea setpriority <id> <priority></red>";

    @ConfigKey("area.invalid-priority")
    public String areaInvalidPriority = "<red>Priority must be a whole number.</red>";

    @ConfigKey("area.priority-set")
    public String areaPrioritySet = "<green>Priority for AFK area '<area>' was set to <priority>.</green>";

    @ConfigKey("status.usage")
    public String statusUsage = "<red>Usage: /afkarea status [player]</red>";

    @ConfigKey("status.player-not-online")
    public String statusPlayerNotOnline = "<red>Player '<player>' is not online.</red>";

    @ConfigKey("status.output")
    public String status = "<gold>AFKArea status for <player></gold>\n"
                    + "<gray>State: <white><state></white>\n"
                    + "Area: <white><area></white>\n"
                    + "Entry: <white><entry></white>\n"
                    + "Idle time: <white><idle></white>\n"
                    + "Reward session: <white><session></white>\n"
                    + "Next reward: <white><next_reward></white>\n"
                    + "IP reward slots: <white><ip_slots></white></gray>";

    @ConfigKey("reload.failed")
    public String reloadFailed = "<red>AFKArea configuration could not be reloaded. The previous configuration remains active. Check the server console.</red>";
}