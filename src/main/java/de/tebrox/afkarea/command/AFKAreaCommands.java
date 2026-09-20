package de.tebrox.afkarea.command;

import de.tebrox.afkarea.area.AreaTeleportService;
import de.tebrox.afkarea.bootstrap.AFKAreaPermissions;
import de.tebrox.afkarea.bootstrap.AFKAreaPlugin;
import de.tebrox.afkarea.command.handler.AreaAdminCommandHandler;
import de.tebrox.afkarea.command.handler.DiagnosticsCommandHandler;
import de.tebrox.afkarea.command.handler.HouseholdCommandHandler;
import de.tebrox.afkarea.command.handler.RewardCommandHandler;
import de.tebrox.afkarea.command.suggestion.AFKAreaCommandSuggestions;
import de.tebrox.vertexCore.command.annotation.*;
import de.tebrox.vertexCore.command.api.CommandContext;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.*;

public final class AFKAreaCommands {
    private final AFKAreaPlugin plugin;

    private final AreaAdminCommandHandler areaAdmin;
    private final DiagnosticsCommandHandler diagnostics;
    private final HouseholdCommandHandler household;
    private final AFKAreaCommandSuggestions suggestions;
    private final RewardCommandHandler rewards;


    public AFKAreaCommands(AFKAreaPlugin plugin) {
        this.plugin = plugin;
        this.areaAdmin = new AreaAdminCommandHandler(plugin);
        this.diagnostics = new DiagnosticsCommandHandler(plugin);
        this.household = new HouseholdCommandHandler(plugin);
        this.suggestions = new AFKAreaCommandSuggestions(plugin);
        this.rewards = new RewardCommandHandler(plugin);
    }

    @VCommand("afkarea")
    @VDesc("Teleport to the default AFK area")
    @VPlayerOnly
    @VPerm(value = AFKAreaPermissions.COMMAND_TELEPORT, def = PermissionDefault.TRUE)
    public void root(CommandContext ctx) {
        Player player = (Player) ctx.sender();

        String areaId = plugin.config().autoTeleportTargetArea;

        if(areaId == null || areaId.isBlank()) {
            plugin.messageService().send(player, plugin.messages().areaUnavailable);
            return;
        }

        AreaTeleportService.Result result = plugin.areaTeleportService().teleport(player, areaId, true);

        if(result == AreaTeleportService.Result.SUCCESS) return;

        plugin.messageService().send(player, plugin.messages().areaUnavailable);
    }

    @VSub("afkarea reload")
    @VDesc("Reload AFKArea configuration")
    @VPerm(AFKAreaPermissions.ADMIN_RELOAD)
    public void reload(CommandContext ctx) {
        if(plugin.reloadConfigs()) {
            plugin.messageService().send(ctx.sender(), plugin.messages().reloadSuccess);
        }else{
            plugin.messageService().send(ctx.sender(), plugin.messages().reloadFailed);
        }
    }

    @VSub("afkarea pos1")
    @VDesc("Set the first cuboid selection position")
    @VPlayerOnly
    @VPerm(AFKAreaPermissions.ADMIN_SELECTION)
    public void pos1(CommandContext ctx) {
        areaAdmin.pos1(ctx);
    }

    @VSub("afkarea pos2")
    @VDesc("Set the second cuboid selection position")
    @VPlayerOnly
    @VPerm(AFKAreaPermissions.ADMIN_SELECTION)
    public void pos2(CommandContext ctx) {
        areaAdmin.pos2(ctx);
    }

    @VSub("afkarea create")
    @VDesc("Create an AFK area")
    @VPlayerOnly
    @VPerm(AFKAreaPermissions.ADMIN_CREATE)
    public void create(CommandContext ctx) {
        areaAdmin.create(ctx);
    }

    @VSub("afkarea setregion")
    @VDesc("Set the WorldGuard region of an AFK area")
    @VPlayerOnly
    @VPerm(AFKAreaPermissions.ADMIN_SET_REGION)
    public void setRegion(CommandContext ctx) {
        areaAdmin.setRegion(ctx);
    }

    @VSub("afkarea redefine")
    @VDesc("Redefine a cuboid AFK area")
    @VPlayerOnly
    @VPerm(AFKAreaPermissions.ADMIN_REDEFINE)
    public void redefine(CommandContext ctx) {
        areaAdmin.redefine(ctx);
    }

    @VSub("afkarea rename")
    @VDesc("Rename an AFK area")
    @VPerm(AFKAreaPermissions.ADMIN_RENAME)
    public void rename(CommandContext ctx) {
        areaAdmin.rename(ctx);
    }

    @VSub("afkarea delete")
    @VDesc("Delete an AFK area")
    @VPerm(AFKAreaPermissions.ADMIN_DELETE)
    public void delete(CommandContext ctx) {
        areaAdmin.delete(ctx);
    }

    @VSub("afkarea setteleport")
    @VDesc("Set the teleport location of an AFK area")
    @VPlayerOnly
    @VPerm(AFKAreaPermissions.ADMIN_SET_TELEPORT)
    public void setTeleport(CommandContext ctx) {
        areaAdmin.setTeleport(ctx);
    }

    @VSub("afkarea tp")
    @VDesc("Teleport to an AFK area")
    @VPlayerOnly
    @VPerm(AFKAreaPermissions.ADMIN_TP)
    public void tp(CommandContext ctx) {
        areaAdmin.tp(ctx);
    }

    @VSub("afkarea list")
    @VDesc("List all AFK areas")
    @VPerm(AFKAreaPermissions.ADMIN_LIST)
    public void list(CommandContext ctx) {
        areaAdmin.list(ctx);
    }

    @VSub("afkarea info")
    @VDesc("Show information about an AFK area")
    @VPerm(AFKAreaPermissions.ADMIN_INFO)
    public void info(CommandContext ctx) {
        areaAdmin.info(ctx);
    }

    @VSub("afkarea setpriority")
    @VDesc("Set the priority of an AFK area")
    @VPerm(AFKAreaPermissions.ADMIN_SET_PRIORITY)
    public void setPriority(CommandContext ctx) {
        areaAdmin.setPriority(ctx);
    }

    @VSub("afkarea status")
    @VDesc("Show runtime AFK status for a player")
    @VPerm(AFKAreaPermissions.ADMIN_STATUS)
    public void status(CommandContext ctx) {
        diagnostics.status(ctx);
    }

    @VSub("afkarea stats")
    @VDesc("Show persistent AFK area statistics for a player")
    @VPerm(AFKAreaPermissions.ADMIN_STATS)
    public void stats(CommandContext ctx) {
        diagnostics.stats(ctx);
    }

    @VSub("afkarea reward info")
    @VDesc("Show the reward configuration of an AFK area")
    @VPerm(AFKAreaPermissions.ADMIN_REWARD)
    public void rewardInfo(CommandContext ctx) {
        rewards.info(ctx);
    }

    @VSub("afkarea reward list")
    @VDesc("List configured rewards of an AFK area")
    @VPerm(AFKAreaPermissions.ADMIN_REWARD)
    public void rewardList(CommandContext ctx) {
        rewards.list(ctx);
    }

    @VSub("afkarea reward add")
    @VDesc("Add a reward to an AFK area")
    @VPerm(AFKAreaPermissions.ADMIN_REWARD)
    public void rewardAdd(CommandContext ctx) {
        rewards.add(ctx);
    }

    @VSub("afkarea reward remove")
    @VDesc("Remove a reward from an AFK area")
    @VPerm(AFKAreaPermissions.ADMIN_REWARD)
    public void rewardRemove(CommandContext ctx) {
        rewards.remove(ctx);
    }

    @VSub("afkarea reward enable")
    @VDesc("Enable or disable a reward")
    @VPerm(AFKAreaPermissions.ADMIN_REWARD)
    public void rewardEnable(CommandContext ctx) {
        rewards.enable(ctx);
    }

    @VSub("afkarea reward command list")
    @VDesc("List commands of a reward")
    @VPerm(AFKAreaPermissions.ADMIN_REWARD)
    public void rewardCommandList(CommandContext ctx) {
        rewards.commandList(ctx);
    }

    @VSub("afkarea reward command add")
    @VDesc("Add a command to a reward")
    @VPerm(AFKAreaPermissions.ADMIN_REWARD)
    public void rewardCommandAdd(CommandContext ctx) {
        rewards.commandAdd(ctx);
    }

    @VSub("afkarea reward command remove")
    @VDesc("Remove a command from a reward")
    @VPerm(AFKAreaPermissions.ADMIN_REWARD)
    public void rewardCommandRemove(CommandContext ctx) {
        rewards.commandRemove(ctx);
    }

    @VSub("afkarea household link")
    @VDesc("Link two player accounts as household members")
    @VPerm(AFKAreaPermissions.ADMIN_HOUSEHOLD)
    public void householdLink(CommandContext ctx) {
        household.link(ctx);
    }

    @VSub("afkarea household unlink")
    @VDesc("Remove a player from their household")
    @VPerm(AFKAreaPermissions.ADMIN_HOUSEHOLD)
    public void householdUnlink(CommandContext ctx) {
        household.unlink(ctx);
    }

    @VSub("afkarea household info")
    @VDesc("Show the household of a player")
    @VPerm(AFKAreaPermissions.ADMIN_HOUSEHOLD)
    public void householdInfo(CommandContext ctx) {
        household.info(ctx);
    }

    @VSub("afkarea household list")
    @VDesc("List all configured households")
    @VPerm(AFKAreaPermissions.ADMIN_HOUSEHOLD)
    public void householdList(CommandContext ctx) {
        household.list(ctx);
    }

    @VSuggest("afkarea redefine")
    public List<String> redefineSuggest(CommandSender sender, String alias, String[] args) {
        return suggestions.areaIds(sender, args, AFKAreaPermissions.ADMIN_REDEFINE);
    }

    @VSuggest("afkarea rename")
    public List<String> renameSuggest(CommandSender sender, String alias, String[] args) {
        return suggestions.areaIds(sender, args, AFKAreaPermissions.ADMIN_RENAME);
    }

    @VSuggest("afkarea delete")
    public List<String> deleteSuggest(CommandSender sender, String alias, String[] args) {
        return suggestions.areaIds(sender, args, AFKAreaPermissions.ADMIN_DELETE);
    }

    @VSuggest("afkarea setteleport")
    public List<String> setTeleportSuggest(CommandSender sender, String alias, String[] args) {
        return suggestions.areaIds(sender, args, AFKAreaPermissions.ADMIN_SET_TELEPORT);
    }

    @VSuggest("afkarea create")
    public List<String> createSuggest(CommandSender sender, String alias, String[] args) {
        return suggestions.createTypes(sender, args, AFKAreaPermissions.ADMIN_CREATE);
    }

    @VSuggest("afkarea tp")
    public List<String> tpSuggest(CommandSender sender, String alias, String[] args) {
        return suggestions.areaIds(sender, args, AFKAreaPermissions.ADMIN_TP);
    }

    @VSuggest("afkarea info")
    public List<String> infoSuggest(CommandSender sender, String alias, String[] args) {
        return suggestions.areaIds(sender, args, AFKAreaPermissions.ADMIN_INFO);
    }

    @VSuggest("afkarea setregion")
    public List<String> setRegionSuggest(CommandSender sender, String alias, String[] args) {
        return suggestions.worldGuardAreaIds(sender, args, AFKAreaPermissions.ADMIN_SET_REGION);
    }

    @VSuggest("afkarea setpriority")
    public List<String> setPrioritySuggest(CommandSender sender, String alias, String[] args) {
        return suggestions.areaIds(sender, args, AFKAreaPermissions.ADMIN_SET_PRIORITY);
    }

    @VSuggest("afkarea status")
    public List<String> statusSuggest(CommandSender sender, String alias, String[] args) {
        return suggestions.onlinePlayers(sender, args, AFKAreaPermissions.ADMIN_STATUS);
    }

    @VSuggest("afkarea stats")
    public List<String> statsSuggest(CommandSender sender, String alias, String[] args) {
        return suggestions.knownPlayers(sender, args, AFKAreaPermissions.ADMIN_STATS);
    }

    @VSuggest("afkarea reward info")
    public List<String> rewardInfoSuggest(CommandSender sender, String alias, String[] args) {
        return suggestions.rewardAreaIds(sender, args, AFKAreaPermissions.ADMIN_REWARD);
    }

    @VSuggest("afkarea reward list")
    public List<String> rewardListSuggest(CommandSender sender, String alias, String[] args) {
        return suggestions.rewardAreaIds(sender, args, AFKAreaPermissions.ADMIN_REWARD);
    }

    @VSuggest("afkarea reward add")
    public List<String> rewardAddSuggest(CommandSender sender, String alias, String[] args) {
        return suggestions.rewardAreaIds(sender, args, AFKAreaPermissions.ADMIN_REWARD);
    }

    @VSuggest("afkarea reward remove")
    public List<String> rewardRemoveSuggest(CommandSender sender, String alias, String[] args) {
        return suggestions.rewardIds(sender, args, AFKAreaPermissions.ADMIN_REWARD);
    }

    @VSuggest("afkarea reward enable")
    public List<String> rewardEnableSuggest(CommandSender sender, String alias, String[] args) {
        return suggestions.rewardEnable(sender, args, AFKAreaPermissions.ADMIN_REWARD);
    }

    @VSuggest("afkarea reward command list")
    public List<String> rewardCommandListSuggest(CommandSender sender, String alias, String[] args) {
        return suggestions.rewardCommandTargets(sender, args, AFKAreaPermissions.ADMIN_REWARD);
    }

    @VSuggest("afkarea reward command add")
    public List<String> rewardCommandAddSuggest(CommandSender sender, String alias, String[] args) {
        return suggestions.rewardCommandTargets(sender, args, AFKAreaPermissions.ADMIN_REWARD);
    }

    @VSuggest("afkarea reward command remove")
    public List<String> rewardCommandRemoveSuggest(CommandSender sender, String alias, String[] args) {
        return suggestions.rewardCommandRemove(sender, args, AFKAreaPermissions.ADMIN_REWARD);
    }
}
