package de.tebrox.afkarea.command;

import de.tebrox.afkarea.area.selection.SelectionPoint;
import de.tebrox.afkarea.area.selection.SelectionService;
import de.tebrox.afkarea.bootstrap.AFKAreaPlugin;
import de.tebrox.afkarea.config.AFKAreaConfig;
import de.tebrox.vertexCore.command.annotation.*;
import de.tebrox.vertexCore.command.api.CommandContext;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;

public final class AFKAreaCommands {
    private final AFKAreaPlugin plugin;

    public AFKAreaCommands(AFKAreaPlugin plugin) {
        this.plugin = plugin;
    }

    @VCommand("afkarea")
    @VDesc("AFKArea commands")
    public void root(CommandContext ctx) {
        ctx.reply("/afkarea reload");
    }

    @VSub("afkarea reload")
    @VDesc("Reload AFKArea configuration")
    @VPerm("afkarea.admin.reload")
    public void reload(CommandContext ctx) {
        plugin.reloadConfigs();

        plugin.messageService().send(ctx.sender(), plugin.messages().reloadSuccess);
    }

    @VSub("afkarea pos1")
    @VDesc("Set the first cuboid selection position")
    @VPlayerOnly
    @VPerm("afkarea.admin.selection")
    public void pos1(CommandContext ctx) {
        Player player = (Player) ctx.sender();

        SelectionPoint point =
                SelectionPoint.from(player.getLocation());

        plugin.selectionService().setPos1(
                player.getUniqueId(),
                point
        );

        sendSelectionPosition(
                player,
                plugin.messages().selectionPos1Set,
                point
        );
    }

    @VSub("afkarea pos2")
    @VDesc("Set the second cuboid selection position")
    @VPlayerOnly
    @VPerm("afkarea.admin.selection")
    public void pos2(CommandContext ctx) {
        Player player = (Player) ctx.sender();

        SelectionPoint point =
                SelectionPoint.from(player.getLocation());

        plugin.selectionService().setPos2(
                player.getUniqueId(),
                point
        );

        sendSelectionPosition(
                player,
                plugin.messages().selectionPos2Set,
                point
        );
    }

    private void sendSelectionPosition(Player player, String message, SelectionPoint point) {
        plugin.messageService().send(
                player,
                message,
                Placeholder.unparsed("world", point.world()),
                Placeholder.unparsed("x", Integer.toString(point.x())),
                Placeholder.unparsed("y", Integer.toString(point.y())),
                Placeholder.unparsed("z", Integer.toString(point.z()))
        );
    }
}
