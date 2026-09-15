package de.tebrox.afkarea.command;

import de.tebrox.afkarea.area.AreaData;
import de.tebrox.afkarea.area.TeleportData;
import de.tebrox.afkarea.area.selection.CuboidSelection;
import de.tebrox.afkarea.area.selection.SelectionPoint;
import de.tebrox.afkarea.bootstrap.AFKAreaPlugin;
import de.tebrox.afkarea.region.data.CuboidRegionData;
import de.tebrox.vertexCore.command.annotation.*;
import de.tebrox.vertexCore.command.api.CommandContext;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

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

    @VSub("afkarea delete")
    @VDesc("Delete an AFK area")
    @VPerm("afkarea.admin.delete")
    public void delete(CommandContext ctx) {
        String[] args = ctx.rawArgs();

        if (args.length < 1) {
            plugin.messageService().send(ctx.sender(), plugin.messages().areaDeleteUsage);
            return;
        }

        String id = args[0];

        if (!plugin.areaManager().hasArea(id)) {
            plugin.messageService().send(ctx.sender(), plugin.messages().unknownArea, Placeholder.unparsed("area", id));
            return;
        }

        plugin.areaManager().deleteArea(
                id,
                () -> plugin.messageService().send(ctx.sender(), plugin.messages().areaDeleted, Placeholder.unparsed("area", id)),
                error -> {
                    plugin.getLogger().severe("Failed to delete AFK area '" + id + "': " + error.getMessage());

                    error.printStackTrace();

                    plugin.messageService().send(ctx.sender(), plugin.messages().areaDeleteFailed, Placeholder.unparsed("area", id));
                }
        );
    }

    @VSub("afkarea setteleport")
    @VDesc("Set the teleport location of an AFK area")
    @VPlayerOnly
    @VPerm("afkarea.admin.setteleport")
    public void setTeleport(CommandContext ctx) {
        Player player = (Player) ctx.sender();
        String[] args = ctx.rawArgs();

        if (args.length < 1) {
            plugin.messageService().send(player, plugin.messages().areaSetTeleportUsage);
            return;
        }

        String id = args[0];

        AreaData current = plugin.areaManager().getArea(id);

        if (current == null) {
            plugin.messageService().send(player, plugin.messages().unknownArea, Placeholder.unparsed("area", id));
            return;
        }

        Location location = player.getLocation();

        TeleportData teleport = new TeleportData(
                location.getWorld().getName(),
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch()
        );

        AreaData updated = current.copy();
        updated.setTeleport(teleport);

        plugin.areaManager().saveArea(
                updated,
                () -> plugin.messageService().send(player, plugin.messages().areaTeleportSet, Placeholder.unparsed("area", id)),
                error -> {
                    plugin.getLogger().severe("Failed to set teleport location for AFK area '" + id + "': " + error.getMessage());

                    error.printStackTrace();

                    plugin.messageService().send(player, plugin.messages().areaSaveFailed, Placeholder.unparsed("area", id));
                }
        );
    }

    @VSub("afkarea tp")
    @VDesc("Teleport to an AFK area")
    @VPlayerOnly
    @VPerm("afkarea.admin.tp")
    public void tp(CommandContext ctx) {
        Player player = (Player) ctx.sender();
        String[] args = ctx.rawArgs();

        if (args.length < 1) {
            plugin.messageService().send(player, plugin.messages().areaTpUsage);
            return;
        }

        String id = args[0];

        AreaData area = plugin.areaManager().getArea(id);

        if (area == null) {
            plugin.messageService().send(player, plugin.messages().unknownArea, Placeholder.unparsed("area", id));
            return;
        }

        TeleportData teleport = area.getTeleport();

        if (teleport == null) {
            plugin.messageService().send(player, plugin.messages().areaTeleportNotSet, Placeholder.unparsed("area", id));
            return;
        }

        World world = Bukkit.getWorld(teleport.getWorld());

        if (world == null) {
            plugin.messageService().send(player, plugin.messages().areaTeleportWorldUnavailable, Placeholder.unparsed("area", id), Placeholder.unparsed("world", teleport.getWorld()));
            return;
        }

        Location target = new Location(world, teleport.getX(), teleport.getY(), teleport.getZ(), teleport.getYaw(), teleport.getPitch());

        if (!player.teleport(target)) {
            plugin.getLogger().warning("Failed to teleport player '" + player.getName() + "' to AFK area '" + id + "'");
            return;
        }

        plugin.messageService().send(player, plugin.messages().areaTeleported, Placeholder.unparsed("area", id));
    }

    @VSub("afkarea list")
    @VDesc("List all AFK areas")
    @VPerm("afkarea.admin.list")
    public void list(CommandContext ctx) {
        List<AreaData> areas = plugin.areaManager().getAreas().stream().sorted(Comparator.comparing(AreaData::getUniqueId, String.CASE_INSENSITIVE_ORDER)).toList();

        if(areas.isEmpty()) {
            plugin.messageService().send(ctx.sender(), plugin.messages().areaListEmpty);
            return;
        }

        String entries = String.join(
                "\n",
                areas.stream().map(area ->
                        "- "
                        + area.getUniqueId()
                        + " - "
                        + area.getName()
                        + " ["
                        + (area.isEnabled() ? "enabled" : "disabled")
                        + "] "
        ).toList());

        plugin.messageService().send(ctx.sender(), plugin.messages().areaList, Placeholder.unparsed("count", Integer.toString(areas.size())), Placeholder.unparsed("areas", entries));
    }

    @VSub("afkarea info")
    @VDesc("Show information about an AFK area")
    @VPerm("afkarea.admin.info")
    public void info(CommandContext ctx) {
        String[] args = ctx.rawArgs();

        if (args.length < 1) {
            plugin.messageService().send(ctx.sender(), plugin.messages().areaInfoUsage);
            return;
        }

        String id = args[0];

        AreaData area = plugin.areaManager().getArea(id);

        if (area == null) {
            plugin.messageService().send(ctx.sender(), plugin.messages().unknownArea, Placeholder.unparsed("area", id));
            return;
        }

        plugin.messageService().send(
                ctx.sender(),
                plugin.messages().areaInfo,
                Placeholder.unparsed(
                        "area",
                        area.getUniqueId()
                ),
                Placeholder.unparsed(
                        "name",
                        area.getName()
                ),
                Placeholder.unparsed(
                        "enabled",
                        Boolean.toString(area.isEnabled())
                ),
                Placeholder.unparsed(
                        "priority",
                        Integer.toString(area.getPriority())
                ),
                Placeholder.unparsed(
                        "region",
                        describeRegion(area)
                ),
                Placeholder.unparsed(
                        "teleport",
                        describeTeleport(area)
                )
        );
    }

    private String describeRegion(AreaData area) {
        if ("cuboid".equalsIgnoreCase(area.getRegionType()) && area.getCuboidRegion() != null) {
            CuboidRegionData region = area.getCuboidRegion();

            return "cuboid "
                    + region.getWorld()
                    + " ["
                    + region.getMinX() + ", "
                    + region.getMinY() + ", "
                    + region.getMinZ()
                    + "] -> ["
                    + region.getMaxX() + ", "
                    + region.getMaxY() + ", "
                    + region.getMaxZ()
                    + "]";
        }

        if (area.getRegionType() == null || area.getRegionType().isBlank()) {
            return "not configured";
        }
        return area.getRegionType();
    }

    private String describeTeleport(AreaData area) {
        TeleportData teleport = area.getTeleport();

        if (teleport == null) {
            return "not set";
        }

        return String.format(Locale.ROOT, "%s %.2f, %.2f, %.2f (yaw %.1f, pitch %.1f)", teleport.getWorld(), teleport.getX(), teleport.getY(), teleport.getZ(), teleport.getYaw(), teleport.getPitch());
    }

    private List<String> suggestAreaIds(CommandSender sender, String[] args, String permission) {
        if(!sender.hasPermission(permission)) {
            return List.of();
        }

        if(args.length > 2) {
            return List.of();
        }

        String token = args.length >= 2 ? args[1] : "";
        String normalized = token.toLowerCase(Locale.ROOT);

        return plugin.areaManager().getAreas().stream().map(AreaData::getUniqueId).filter(id -> id != null && !id.isBlank()).filter(id -> id.toLowerCase(Locale.ROOT).startsWith(normalized)).sorted(String.CASE_INSENSITIVE_ORDER).toList();
    }

    @VSuggest("afkarea redefine")
    public List<String> redefineSuggest(CommandSender sender, String alias, String[] args) {
        return suggestAreaIds(sender, args, "afkarea.admin.redefine");
    }

    @VSuggest("afkarea rename")
    public List<String> renameSuggest(CommandSender sender, String alias, String[] args) {
        return suggestAreaIds(sender, args, "afkarea.admin.rename");
    }

    @VSuggest("afkarea delete")
    public List<String> deleteSuggest(CommandSender sender, String alias, String[] args) {
        return suggestAreaIds(sender, args, "afkarea.admin.delete");
    }

    @VSuggest("afkarea setteleport")
    public List<String> setTeleportSuggest(CommandSender sender, String alias, String[] args) {
        return suggestAreaIds(sender, args, "afkarea.admin.setteleport");
    }

    @VSuggest("afkarea create")
    public List<String> createSuggest(CommandSender sender, String alias, String[] args) {
        if(!sender.hasPermission("afkarea.admin.create")) {
            return List.of();
        }

        if(args.length != 3) {
            return List.of();
        }

        String token = args[2].toLowerCase(Locale.ROOT);

        return List.of("cuboid").stream().filter(type -> type.startsWith(token)).toList();
    }

    @VSuggest("afkarea tp")
    public List<String> tpSuggest(CommandSender sender, String alias, String[] args) {
        return suggestAreaIds(sender, args, "afkarea.admin.tp");
    }

    @VSuggest("afkarea info")
    public List<String> infoSuggest(CommandSender sender, String alias, String[] args) {
        return suggestAreaIds(sender, args, "afkarea.admin.info");
    }
}
