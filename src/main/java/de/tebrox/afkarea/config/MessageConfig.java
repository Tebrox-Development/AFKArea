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

    @ConfigKey("stats.usage")
    public String statsUsage = "<red>Usage: /afkarea stats <player></red>";

    @ConfigKey("stats.data-loading")
    public String statsDataLoading = "<yellow>AFK player statistics are still loading. Please try again.</yellow>";

    @ConfigKey("stats.unknown-player")
    public String statsUnknownPlayer = "<red>Player '<player>' is not known to this server.</red>";

    @ConfigKey("stats.no-data")
    public String statsNoData = "<yellow>No completed AFK area sessions are stored for <player>.</yellow>";

    @ConfigKey("stats.output")
    public String stats = "<gold>AFKArea stats for <player></gold>\n"
                    + "<gray>Sessions: <white><sessions></white>\n"
                    + "Total time: <white><total_time></white>\n"
                    + "Longest session: <white><longest_session></white>\n"
                    + "Last session: <white><last_session></white>\n"
                    + "Last area: <white><last_area></white>\n"
                    + "Last session ended: <white><last_ended></white></gray>";

    @ConfigKey("display.bossbar.text")
    public String bossBarText = "<gold>AFK Area</gold> <gray>•</gray> <yellow><session></yellow>";

    @ConfigKey("display.actionbar.text")
    public String actionBarText = "<gray>Next reward in <aqua><next_reward></aqua></gray>";

    @ConfigKey("display.actionbar.no-reward-text")
    public String actionBarNoRewardText = "<gray>AFK Area <yellow><session></yellow></gray>";

    @ConfigKey("reward.admin.info-usage")
    public String rewardAdminInfoUsage = "<red>Usage: /afkarea reward info <area></red>";

    @ConfigKey("reward.admin.list-usage")
    public String rewardAdminListUsage = "<red>Usage: /afkarea reward list <area></red>";

    @ConfigKey("reward.admin.add-usage")
    public String rewardAdminAddUsage = "<red>Usage: /afkarea reward add <area> <id></red>";

    @ConfigKey("reward.admin.remove-usage")
    public String rewardAdminRemoveUsage = "<red>Usage: /afkarea reward remove <area> <id></red>";

    @ConfigKey("reward.admin.no-config")
    public String rewardAdminNoConfig = "<yellow>AFK area '<area>' has no reward configuration.</yellow>";

    @ConfigKey("reward.admin.info")
    public String rewardAdminInfo = "<gold>Reward configuration for '<area>'</gold>\n"
                    + "<gray>Schedule: <white><schedule></white>\n"
                    + "Interval: <white><interval></white>\n"
                    + "Rolls: <white><rolls></white>\n"
                    + "Allow duplicates: <white><duplicates></white>\n"
                    + "Rewards: <white><reward_count></white>\n"
                    + "Milestones: <white><milestone_count></white>\n"
                    + "Default message: <white><message></white></gray>";

    @ConfigKey("reward.admin.list-empty")
    public String rewardAdminListEmpty = "<yellow>AFK area '<area>' has no configured rewards.</yellow>";

    @ConfigKey("reward.admin.list")
    public String rewardAdminList = "<gold>Rewards for '<area>' (<count>):</gold>\n"
            + "<gray><rewards></gray>";

    @ConfigKey("reward.admin.invalid-id")
    public String rewardAdminInvalidId = "<red>Reward IDs may only contain lowercase letters, numbers, '-' and '_'.</red>";

    @ConfigKey("reward.admin.already-exists")
    public String rewardAdminAlreadyExists = "<red>Reward '<reward>' already exists in AFK area '<area>'.</red>";

    @ConfigKey("reward.admin.unknown")
    public String rewardAdminUnknown = "<red>Reward '<reward>' does not exist in AFK area '<area>'.</red>";

    @ConfigKey("reward.admin.added")
    public String rewardAdminAdded = "<green>Added reward '<reward>' to AFK area '<area>'. The reward is disabled until configured.</green>";

    @ConfigKey("reward.admin.removed")
    public String rewardAdminRemoved = "<green>Removed reward '<reward>' from AFK area '<area>'.</green>";

    @ConfigKey("reward.admin.enable-usage")
    public String rewardAdminEnableUsage = "<red>Usage: /afkarea reward enable <area> <id> <true|false></red>";

    @ConfigKey("reward.admin.invalid-enabled")
    public String rewardAdminInvalidEnabled = "<red>Enabled must be either true or false.</red>";

    @ConfigKey("reward.admin.cannot-enable")
    public String rewardAdminCannotEnable = "<red>Reward '<reward>' cannot be enabled because it has no commands.</red>";

    @ConfigKey("reward.admin.enabled")
    public String rewardAdminEnabled = "<green>Reward '<reward>' in AFK area '<area>' enabled: <enabled>.</green>";

    @ConfigKey("reward.admin.command-list-usage")
    public String rewardAdminCommandListUsage = "<red>Usage: /afkarea reward command list <area> <id></red>";

    @ConfigKey("reward.admin.command-add-usage")
    public String rewardAdminCommandAddUsage = "<red>Usage: /afkarea reward command add <area> <id> <command...></red>";

    @ConfigKey("reward.admin.command-remove-usage")
    public String rewardAdminCommandRemoveUsage = "<red>Usage: /afkarea reward command remove <area> <id> <index></red>";

    @ConfigKey("reward.admin.command-list-empty")
    public String rewardAdminCommandListEmpty = "<yellow>Reward '<reward>' has no commands.</yellow>";

    @ConfigKey("reward.admin.command-list")
    public String rewardAdminCommandList = "<gold>Commands for reward '<reward>' in '<area>':</gold>\n"
                    + "<gray><commands></gray>";

    @ConfigKey("reward.admin.command-added")
    public String rewardAdminCommandAdded = "<green>Added command to reward '<reward>': <white><command></white></green>";

    @ConfigKey("reward.admin.command-invalid-index")
    public String rewardAdminCommandInvalidIndex = "<red>Command index must reference an existing reward command.</red>";

    @ConfigKey("reward.admin.command-removed")
    public String rewardAdminCommandRemoved = "<green>Removed command from reward '<reward>': <white><command></white></green>";

    @ConfigKey("reward.admin.save-failed")
    public String rewardAdminSaveFailed = "<red>Failed to update rewards for AFK area '<area>'. Check the server console.</red>";

    @ConfigKey("reward.admin.weight-usage")
    public String rewardAdminWeightUsage = "<red>Usage: /afkarea reward weight <area> <id> <weight></red>";

    @ConfigKey("reward.admin.invalid-weight")
    public String rewardAdminInvalidWeight = "<red>Reward weight must be a finite number greater than 0.</red>";

    @ConfigKey("reward.admin.weight-set")
    public String rewardAdminWeightSet = "<green>Set weight of reward '<reward>' in AFK area '<area>' to <weight>.</green>";

    @ConfigKey("reward.admin.permission-usage")
    public String rewardAdminPermissionUsage = "<red>Usage: /afkarea reward permission <area> <id> <permission|none></red>";

    @ConfigKey("reward.admin.permission-set")
    public String rewardAdminPermissionSet = "<green>Set permission of reward '<reward>' in AFK area '<area>' to '<permission>'.</green>";

    @ConfigKey("reward.admin.message-usage")
    public String rewardAdminMessageUsage = "<red>Usage: /afkarea reward message <area> <id> <message|none></red>";

    @ConfigKey("reward.admin.message-set")
    public String rewardAdminMessageSet = "<green>Updated the message of reward '<reward>' in AFK area '<area>': <message></green>";

    @ConfigKey("reward.admin.interval-usage")
    public String rewardAdminIntervalUsage = "<red>Usage: /afkarea reward interval <area> <seconds> [rolls]</red>";

    @ConfigKey("reward.admin.invalid-seconds")
    public String rewardAdminInvalidSeconds = "<red>Seconds must be a whole number greater than 0.</red>";

    @ConfigKey("reward.admin.invalid-rolls")
    public String rewardAdminInvalidRolls = "<red>Rolls must be a whole number greater than 0.</red>";

    @ConfigKey("reward.admin.interval-set")
    public String rewardAdminIntervalSet = "<green>Set reward schedule of AFK area '<area>' to every <seconds> seconds with <rolls> roll(s).</green>";

    @ConfigKey("reward.admin.milestone-list-usage")
    public String rewardAdminMilestoneListUsage = "<red>Usage: /afkarea reward milestone list <area></red>";

    @ConfigKey("reward.admin.milestone-add-usage")
    public String rewardAdminMilestoneAddUsage = "<red>Usage: /afkarea reward milestone add <area> <seconds> [rolls]</red>";

    @ConfigKey("reward.admin.milestone-remove-usage")
    public String rewardAdminMilestoneRemoveUsage = "<red>Usage: /afkarea reward milestone remove <area> <seconds></red>";

    @ConfigKey("reward.admin.milestone-list-empty")
    public String rewardAdminMilestoneListEmpty = "<yellow>AFK area '<area>' has no configured reward milestones.</yellow>";

    @ConfigKey("reward.admin.milestone-list")
    public String rewardAdminMilestoneList = "<gold>Reward milestones for '<area>' (<count>):</gold>\n"
                    + "<gray><milestones></gray>";

    @ConfigKey("reward.admin.milestone-exists")
    public String rewardAdminMilestoneExists = "<red>A reward milestone already exists at <seconds> seconds.</red>";

    @ConfigKey("reward.admin.milestone-added")
    public String rewardAdminMilestoneAdded = "<green>Added reward milestone at <seconds> seconds with <rolls> roll(s) to AFK area '<area>'.</green>";

    @ConfigKey("reward.admin.milestone-not-found")
    public String rewardAdminMilestoneNotFound = "<red>No reward milestone exists at <seconds> seconds.</red>";

    @ConfigKey("reward.admin.milestone-last-active")
    public String rewardAdminMilestoneLastActive = "<red>The last milestone cannot be removed while the milestone schedule is active. Switch the area to an interval schedule first.</red>";

    @ConfigKey("reward.admin.milestone-removed")
    public String rewardAdminMilestoneRemoved = "<green>Removed the reward milestone at <seconds> seconds from AFK area '<area>'.</green>";

    @ConfigKey("reward.admin.duplicates-usage")
    public String rewardAdminDuplicatesUsage = "<red>Usage: /afkarea reward duplicates <area> <true|false></red>";

    @ConfigKey("reward.admin.invalid-duplicates")
    public String rewardAdminInvalidDuplicates = "<red>Allow duplicates must be either true or false.</red>";

    @ConfigKey("reward.admin.duplicates-set")
    public String rewardAdminDuplicatesSet = "<green>Allow duplicate reward selections for AFK area '<area>': <duplicates>.</green>";

    @ConfigKey("reward.admin.default-message-usage")
    public String rewardAdminDefaultMessageUsage = "<red>Usage: /afkarea reward defaultmessage <area> <message|none></red>";

    @ConfigKey("reward.admin.default-message-set")
    public String rewardAdminDefaultMessageSet = "<green>Updated the default reward message of AFK area '<area>': <message></green>";

    @ConfigKey("display.admin.usage")
    public String displayAdminUsage = "<red>Usage: /afkarea display <bossbar|actionbar> <on|off></red>";

    @ConfigKey("display.admin.invalid-type")
    public String displayAdminInvalidType = "<red>Display type must be either bossbar or actionbar.</red>";

    @ConfigKey("display.admin.invalid-state")
    public String displayAdminInvalidState = "<red>Display state must be either on or off.</red>";

    @ConfigKey("display.admin.updated")
    public String displayAdminUpdated = "<green>AFKArea <display> display is now <state>.</green>";

    @ConfigKey("display.admin.save-failed")
    public String displayAdminSaveFailed = "<red>Failed to save the display setting. Check the server console.</red>";

    @ConfigKey("area.includechildren-usage")
    public String areaIncludeChildrenUsage = "<red>Usage: /afkarea includechildren <id> <yes|no></red>";

    @ConfigKey("area.includechildren-invalid")
    public String areaIncludeChildrenInvalid = "<red>Include children must be either yes or no.</red>";

    @ConfigKey("area.includechildren-set")
    public String areaIncludeChildrenSet = "<green>Include child regions for AFK area '<area>' is now <value>.</green>";
}