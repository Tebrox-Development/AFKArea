package de.tebrox.afkarea.command;

import de.tebrox.afkarea.bootstrap.AFKAreaPermissions;
import de.tebrox.afkarea.bootstrap.AFKAreaPlugin;
import de.tebrox.afkarea.state.PlayerState;
import de.tebrox.vertexCore.command.annotation.VCommand;
import de.tebrox.vertexCore.command.annotation.VDesc;
import de.tebrox.vertexCore.command.annotation.VPerm;
import de.tebrox.vertexCore.command.annotation.VPlayerOnly;
import de.tebrox.vertexCore.command.api.CommandContext;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

public final class AfkCommand {

    private final AFKAreaPlugin plugin;
    public AfkCommand(AFKAreaPlugin plugin) {
        this.plugin = plugin;
    }

    @VCommand("afk")
    @VDesc("Toggle your AFK status")
    @VPlayerOnly
    @VPerm(value = AFKAreaPermissions.COMMAND_AFK, def = PermissionDefault.TRUE)
    public void afk(CommandContext ctx) {
        Player player = (Player) ctx.sender();
        PlayerState state = plugin.playerStateService().toggleManualAfk(player.getUniqueId());

        if(state == PlayerState.AFK) {
            plugin.tabListService().applyAfk(player);
            plugin.messageService().send(player, plugin.messages().afkEnabled);
        }else if(state == PlayerState.ACTIVE) {
            plugin.tabListService().clearAfk(player);
            plugin.messageService().send(player, plugin.messages().afkDisabled);
        }
    }
}
