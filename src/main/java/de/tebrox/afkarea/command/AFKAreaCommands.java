package de.tebrox.afkarea.command;

import de.tebrox.afkarea.area.AreaData;
import de.tebrox.afkarea.area.selection.CuboidSelection;
import de.tebrox.afkarea.area.selection.SelectionPoint;
import de.tebrox.afkarea.bootstrap.AFKAreaPlugin;
import de.tebrox.afkarea.region.data.CuboidRegionData;
import de.tebrox.vertexCore.command.annotation.*;
import de.tebrox.vertexCore.command.api.CommandContext;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Level;

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

    @VSub("afkarea create")
    @VDesc("Create an AFK area")
    @VPlayerOnly
    @VPerm("afkarea.admin.create")
    public void create(CommandContext ctx) {
        Player player = (Player) ctx.sender();
        String[] args = ctx.rawArgs();

        // rawArgs:
        // /afkarea create spawn-afk cuboid
        // -> ["spawn-afk", "cuboid"]
        if (args.length < 2) {
            plugin.messageService().send(player, plugin.messages().areaCreateUsage);
            return;
        }

        String id = args[0];
        String type = args[1].toLowerCase(Locale.ROOT);

        if (!id.matches("[a-z0-9][a-z0-9_-]*")) {
            plugin.messageService().send(player, plugin.messages().areaInvalidId);
            return;
        }

        if (plugin.areaManager().hasArea(id)) {
            plugin.messageService().send(player, plugin.messages().areaAlreadyExists, Placeholder.unparsed("area", id));
            return;
        }

        if (!type.equals("cuboid")) {
            plugin.messageService().send(player, plugin.messages().areaUnsupportedRegionType, Placeholder.unparsed("type", type));
            return;
        }

        CuboidSelection selection =
                plugin.selectionService().getSelection(player.getUniqueId());

        if (selection == null || !selection.isComplete()) {
            plugin.messageService().send(player, plugin.messages().areaSelectionIncomplete);
            return;
        }

        if (!selection.isSameWorld()) {
            plugin.messageService().send(player, plugin.messages().areaSelectionWorldMismatch);
            return;
        }

        createCuboidArea(player, id, selection);
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

    private void createCuboidArea(Player player, String id, CuboidSelection selection) {
        SelectionPoint pos1 = selection.pos1();
        SelectionPoint pos2 = selection.pos2();

        CuboidRegionData region = new CuboidRegionData(pos1.world(), pos1.x(), pos1.y(), pos1.z(), pos2.x(), pos2.y(), pos2.z());
        AreaData area = new AreaData(id, id);
        area.setRegionType("cuboid");
        area.setCuboidRegion(region);

        plugin.areaManager().saveArea(
                area,
                () -> plugin.messageService().send(
                        player,
                        plugin.messages().areaCreated,
                        Placeholder.unparsed("area", id)
                ),
                error -> {
                    plugin.getLogger().severe("Failed to create AFK area '" + id + "': " + error.getMessage());

                    error.printStackTrace();

                    plugin.messageService().send(player, plugin.messages().areaSaveFailed, Placeholder.unparsed("area", id));
                }
        );
    }

    @VSub("afkarea redefine")
    @VDesc("Redefine a cuboid AFK area")
    @VPlayerOnly
    @VPerm("afkarea.admin.redefine")
    public void redefine(CommandContext ctx) {
        Player player = (Player) ctx.sender();
        String[] args = ctx.rawArgs();

        if (args.length < 1) {
            plugin.messageService().send(player, plugin.messages().areaRedefineUsage);
            return;
        }

        String id = args[0];

        AreaData current = plugin.areaManager().getArea(id);

        if (current == null) {
            plugin.messageService().send(player, plugin.messages().unknownArea, Placeholder.unparsed("area", id));
            return;
        }

        if (!"cuboid".equalsIgnoreCase(current.getRegionType())) {
            plugin.messageService().send(player, plugin.messages().areaNotCuboid, Placeholder.unparsed("area", id));
            return;
        }

        CuboidSelection selection =
                plugin.selectionService().getSelection( player.getUniqueId());

        if (selection == null || !selection.isComplete()) {
            plugin.messageService().send(player, plugin.messages().areaSelectionIncomplete);
            return;
        }

        if (!selection.isSameWorld()) {
            plugin.messageService().send(player, plugin.messages().areaSelectionWorldMismatch);
            return;
        }

        redefineCuboidArea(player, current, selection);
    }

    private void redefineCuboidArea(Player player, AreaData current, CuboidSelection selection) {
        SelectionPoint pos1 = selection.pos1();
        SelectionPoint pos2 = selection.pos2();

        CuboidRegionData region = new CuboidRegionData(pos1.world(), pos1.x(), pos1.y(), pos1.z(), pos2.x(), pos2.y(), pos2.z());

        AreaData updated = current.copy();
        updated.setCuboidRegion(region);

        String id = updated.getUniqueId();

        plugin.areaManager().saveArea(
                updated,
                () -> plugin.messageService().send(player, plugin.messages().areaRedefined, Placeholder.unparsed("area", id)),
                error -> {
                    plugin.getLogger().severe("Failed to redefine AFK area '" + id + "': " + error.getMessage());

                    error.printStackTrace();

                    plugin.messageService().send(player, plugin.messages().areaSaveFailed, Placeholder.unparsed("area", id));
                }
        );
    }

    @VSub("afkarea rename")
    @VDesc("Rename an AFK area")
    @VPerm("afkarea.admin.rename")
    public void rename(CommandContext ctx) {
        String[] args = ctx.rawArgs();

        if (args.length < 2) {
            plugin.messageService().send(ctx.sender(), plugin.messages().areaRenameUsage);
            return;
        }

        String id = args[0];

        AreaData current = plugin.areaManager().getArea(id);

        if (current == null) {
            plugin.messageService().send( ctx.sender(), plugin.messages().unknownArea, Placeholder.unparsed("area", id));
            return;
        }

        String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim();

        if (name.isEmpty()) {
            plugin.messageService().send(ctx.sender(), plugin.messages().areaInvalidName);
            return;
        }

        AreaData updated = current.copy();
        updated.setName(name);

        plugin.areaManager().saveArea(
                updated,
                () -> plugin.messageService().send(ctx.sender(), plugin.messages().areaRenamed, Placeholder.unparsed("area", id), Placeholder.unparsed("name", name)),
                error -> {
                    plugin.getLogger().severe("Failed to rename AFK area '" + id + "': " + error.getMessage());

                    error.printStackTrace();

                    plugin.messageService().send(ctx.sender(), plugin.messages().areaSaveFailed, Placeholder.unparsed("area", id));
                }
        );
    }
}
