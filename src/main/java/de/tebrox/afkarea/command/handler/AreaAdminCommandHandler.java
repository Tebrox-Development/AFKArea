package de.tebrox.afkarea.command.handler;

import de.tebrox.afkarea.area.AreaData;
import de.tebrox.afkarea.area.AreaTeleportService;
import de.tebrox.afkarea.area.TeleportData;
import de.tebrox.afkarea.area.selection.CuboidSelection;
import de.tebrox.afkarea.area.selection.SelectionPoint;
import de.tebrox.afkarea.bootstrap.AFKAreaPlugin;
import de.tebrox.afkarea.region.data.CuboidRegionData;
import de.tebrox.afkarea.region.data.WorldGuardRegionData;
import de.tebrox.vertexCore.command.api.CommandContext;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class AreaAdminCommandHandler {
    private final AFKAreaPlugin plugin;

    public AreaAdminCommandHandler(AFKAreaPlugin plugin) {
        this.plugin = plugin;
    }

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

        if(type.equals("cuboid")) {
            CuboidSelection selection = plugin.selectionService().getSelection(player.getUniqueId());

            if (selection == null || !selection.isComplete()) {
                plugin.messageService().send(player, plugin.messages().areaSelectionIncomplete);
                return;
            }

            if (!selection.isSameWorld()) {
                plugin.messageService().send(player, plugin.messages().areaSelectionWorldMismatch);
                return;
            }

            createCuboidArea(player, id, selection);
            return;
        }

        if(type.equals("worldguard")) {
            if(!plugin.worldGuardIntegration().isAvailable()) {
                plugin.messageService().send(player, plugin.messages().worldGuardUnavailable);
                return;
            }

            if(args.length < 3 || args[2].isBlank()) {
                plugin.messageService().send(player, plugin.messages().areaCreateWorldGuardUsage);
                return;
            }

            String regionId = args[2];
            String worldName = player.getWorld().getName();

            if(!plugin.areaManager().worldGuardRegionExists(worldName, regionId)) {
                plugin.messageService().send(player, plugin.messages().worldGuardRegionNotFound, Placeholder.unparsed("region", regionId), Placeholder.unparsed("world", worldName));
                return;
            }

            createWorldGuardArea(player, id, args[2]);
            return;
        }

        plugin.messageService().send(player, plugin.messages().areaUnsupportedRegionType, Placeholder.unparsed("type", type));
    }

    public void setRegion(CommandContext ctx) {
        Player player = (Player) ctx.sender();
        String[] args = ctx.rawArgs();

        if(args.length < 2) {
            plugin.messageService().send(player, plugin.messages().areaSetRegionUsage);
            return;
        }

        if(!plugin.worldGuardIntegration().isAvailable()) {
            plugin.messageService().send(player, plugin.messages().worldGuardUnavailable);
            return;
        }

        String id = args[0];
        String regionId = args[1];

        AreaData current = plugin.areaManager().getArea(id);

        if(current == null) {
            plugin.messageService().send(player, plugin.messages().unknownArea, Placeholder.unparsed("area", id));
            return;
        }

        if(!"worldguard".equalsIgnoreCase(current.getRegionType())) {
            plugin.messageService().send(player, plugin.messages().areaNotWorldGuard, Placeholder.unparsed("area", id));
            return;
        }

        String worldName = player.getWorld().getName();
        if(!plugin.areaManager().worldGuardRegionExists(worldName, regionId)) {
            plugin.messageService().send(player, plugin.messages().worldGuardRegionNotFound, Placeholder.unparsed("region", regionId), Placeholder.unparsed("world", worldName));
            return;
        }

        WorldGuardRegionData currentRegion = current.getWorldGuardRegion();
        boolean includeChildren = currentRegion != null && currentRegion.isIncludeChildren();

        WorldGuardRegionData region = new WorldGuardRegionData(worldName, regionId, includeChildren);
        AreaData updated = current.copy();
        updated.setWorldGuardRegion(region);

        plugin.areaManager().saveArea(updated, () -> plugin.messageService().send(player, plugin.messages().areaRegionSet, Placeholder.unparsed("area", id), Placeholder.unparsed("region", regionId), Placeholder.unparsed("world", player.getWorld().getName())),
                error -> {
                    plugin.getLogger().severe("Failed to update WorldGuard region for AFK area '" + id + "': " + error.getMessage());
                    error.printStackTrace();
                    plugin.messageService().send(player, plugin.messages().areaSaveFailed, Placeholder.unparsed("area", id));
                });
    }

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

        AreaTeleportService.Result result = plugin.areaTeleportService().teleport(player, id, false);

        switch (result) {
            case SUCCESS -> plugin.messageService().send(player, plugin.messages().areaTeleported, Placeholder.unparsed("area", id));
            case TELEPORT_NOT_SET -> plugin.messageService().send(player, plugin.messages().areaTeleportNotSet, Placeholder.unparsed("area", id));
            case WORLD_UNAVAILABLE -> {
                TeleportData teleport = area.getTeleport();
                plugin.messageService().send(player, plugin.messages().areaTeleportWorldUnavailable, Placeholder.unparsed("area", id), Placeholder.unparsed("world", teleport == null ? "unknown" : teleport.getWorld()));
            }
            case TELEPORT_FAILED -> plugin.getLogger().warning("Failed to teleport player '" + player.getName() + "' to AFK area '" + id + "'");
            case AREA_NOT_FOUND, AREA_DISABLED -> {}
        }
    }

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

    public void setPriority(CommandContext ctx) {
        String[] args = ctx.rawArgs();
        if(args.length < 2) {
            plugin.messageService().send(ctx.sender(), plugin.messages().areaSetPriorityUsage);
            return;
        }

        String id = args[0];
        AreaData current = plugin.areaManager().getArea(id);
        if(current == null) {
            plugin.messageService().send(ctx.sender(), plugin.messages().unknownArea, Placeholder.unparsed("area", id));
            return;
        }

        int priority;
        try {
            priority = Integer.parseInt(args[1]);
        }catch(NumberFormatException exception) {
            plugin.messageService().send(ctx.sender(), plugin.messages().areaInvalidPriority);
            return;
        }

        AreaData updated = current.copy();
        updated.setPriority(priority);
        plugin.areaManager().saveArea(updated,
                () -> plugin.messageService().send(ctx.sender(), plugin.messages().areaPrioritySet, Placeholder.unparsed("area", id), Placeholder.unparsed("priority", Integer.toString(priority))),
                error -> {
                    plugin.getLogger().severe("Failed to update priority for AFK area '" + id + "': " + error.getMessage());
                    error.printStackTrace();
                    plugin.messageService().send(ctx.sender(), plugin.messages().areaSaveFailed, Placeholder.unparsed("area", id));
                });
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

    private void createWorldGuardArea(Player player, String id, String regionId) {
        WorldGuardRegionData region = new WorldGuardRegionData(player.getWorld().getName(), regionId, false);
        AreaData area = new AreaData(id, id);
        area.setRegionType("worldguard");
        area.setWorldGuardRegion(region);

        plugin.areaManager().saveArea(area, () -> plugin.messageService().send(player, plugin.messages().areaCreated, Placeholder.unparsed("area", id)), error -> {
            plugin.getLogger().severe("Failed to create AFK area '" + id +"': " + error.getMessage());
            error.printStackTrace();
            plugin.messageService().send(player, plugin.messages().areaSaveFailed, Placeholder.unparsed("area", id));
        });
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

        if("worldguard".equalsIgnoreCase(area.getRegionType()) && area.getWorldGuardRegion() != null) {
            WorldGuardRegionData region = area.getWorldGuardRegion();

            return "worldguard "
                    + region.getWorld()
                    + ":"
                    + region.getRegionId()
                    + " (include children: "
                    + (region.isIncludeChildren() ? "yes" : "no")
                    + ")";
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
}
