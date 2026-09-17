package de.tebrox.afkarea.command;

import de.tebrox.afkarea.area.AreaData;
import de.tebrox.afkarea.area.AreaTeleportService;
import de.tebrox.afkarea.area.TeleportData;
import de.tebrox.afkarea.area.selection.CuboidSelection;
import de.tebrox.afkarea.area.selection.SelectionPoint;
import de.tebrox.afkarea.bootstrap.AFKAreaPlugin;
import de.tebrox.afkarea.region.WorldGuardRegionProvider;
import de.tebrox.afkarea.region.data.CuboidRegionData;
import de.tebrox.afkarea.region.data.WorldGuardRegionData;
import de.tebrox.vertexCore.command.annotation.*;
import de.tebrox.vertexCore.command.api.CommandContext;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.*;

import de.tebrox.afkarea.household.HouseholdManager;

public final class AFKAreaCommands {
    private final AFKAreaPlugin plugin;

    public AFKAreaCommands(AFKAreaPlugin plugin) {
        this.plugin = plugin;
    }

    @VCommand("afkarea")
    @VDesc("Teleport to the default AFK area")
    @VPlayerOnly
    @VPerm(value = "afkarea.command.teleport", def = PermissionDefault.TRUE)
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

    @VSub("afkarea setregion")
    @VDesc("Set the WorldGuard region of an AFK area")
    @VPlayerOnly
    @VPerm("afkarea.admin.setregion")
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

    @VSub("afkarea household link")
    @VDesc("Link two player accounts as household members")
    @VPerm("afkarea.admin.household")
    public void householdLink(CommandContext ctx) {
        String[] args = ctx.rawArgs();

        if (args.length < 2) {
            plugin.messageService().send(ctx.sender(), plugin.messages().householdLinkUsage);
            return;
        }

        String firstInput = args[0];
        String secondInput = args[1];

        OfflinePlayer first = findKnownPlayer(firstInput);

        if (first == null) {
            plugin.messageService().send(ctx.sender(), plugin.messages().householdUnknownPlayer, Placeholder.unparsed("player", firstInput));
            return;
        }

        OfflinePlayer second = findKnownPlayer(secondInput);

        if (second == null) {
            plugin.messageService().send(ctx.sender(), plugin.messages().householdUnknownPlayer, Placeholder.unparsed("player", secondInput));
            return;
        }

        String firstName = first.getName() == null ? firstInput : first.getName();
        String secondName = second.getName() == null ? secondInput : second.getName();

        plugin.householdManager().link(
                first.getUniqueId(),
                second.getUniqueId(),
                result -> {
                    switch (result) {
                        case LINKED -> plugin.messageService().send(ctx.sender(), plugin.messages().householdLinked, Placeholder.unparsed("player1", firstName), Placeholder.unparsed("player2", secondName));

                        case SAME_PLAYER -> plugin.messageService().send(ctx.sender(), plugin.messages().householdSamePlayer);
                        case ALREADY_SAME_HOUSEHOLD -> plugin.messageService().send(ctx.sender(), plugin.messages().householdAlreadySame, Placeholder.unparsed("player1", firstName), Placeholder.unparsed("player2", secondName));
                        case DIFFERENT_HOUSEHOLDS -> plugin.messageService().send(ctx.sender(), plugin.messages().householdDifferentHouseholds, Placeholder.unparsed("player1", firstName), Placeholder.unparsed("player2", secondName));
                        case DATA_LOADING -> plugin.messageService().send(ctx.sender(), plugin.messages().householdDataLoading);
                        case BUSY -> plugin.messageService().send(ctx.sender(), plugin.messages().householdBusy);
                    }
                },
                error -> {
                    plugin.getLogger().severe("Failed to link household members: " + error.getMessage());
                    error.printStackTrace();

                    plugin.messageService().send(ctx.sender(), plugin.messages().householdSaveFailed);
                }
        );
    }

    @VSub("afkarea household unlink")
    @VDesc("Remove a player from their household")
    @VPerm("afkarea.admin.household")
    public void householdUnlink(CommandContext ctx) {
        String[] args = ctx.rawArgs();

        if (args.length < 1) {
            plugin.messageService().send(ctx.sender(), plugin.messages().householdUnlinkUsage);
            return;
        }

        String input = args[0];

        OfflinePlayer player = findKnownPlayer(input);

        if (player == null) {
            plugin.messageService().send(ctx.sender(), plugin.messages().householdUnknownPlayer, Placeholder.unparsed("player", input));
            return;
        }

        String name = player.getName() == null ? input : player.getName();

        plugin.householdManager().unlink(
                player.getUniqueId(),
                result -> {
                    switch (result) {
                        case UNLINKED -> plugin.messageService().send(ctx.sender(), plugin.messages().householdUnlinked, Placeholder.unparsed("player", name));
                        case NOT_LINKED -> plugin.messageService().send(ctx.sender(), plugin.messages().householdNotLinked, Placeholder.unparsed("player", name));
                        case DATA_LOADING -> plugin.messageService().send(ctx.sender(), plugin.messages().householdDataLoading);
                        case BUSY -> plugin.messageService().send(ctx.sender(), plugin.messages().householdBusy);
                    }
                },
                error -> {
                    plugin.getLogger().severe("Failed to unlink household member: " + error.getMessage());

                    error.printStackTrace();
                    plugin.messageService().send(ctx.sender(), plugin.messages().householdSaveFailed);
                }
        );
    }

    @VSub("afkarea household info")
    @VDesc("Show the household of a player")
    @VPerm("afkarea.admin.household")
    public void householdInfo(CommandContext ctx) {
        String[] args = ctx.rawArgs();

        if (args.length < 1) {
            plugin.messageService().send(ctx.sender(), plugin.messages().householdInfoUsage);
            return;
        }

        if (!plugin.householdManager().isLoaded()) {
            plugin.messageService().send(ctx.sender(), plugin.messages().householdDataLoading);
            return;
        }

        String input = args[0];
        OfflinePlayer player = findKnownPlayer(input);

        if (player == null) {
            plugin.messageService().send(ctx.sender(), plugin.messages().householdUnknownPlayer, Placeholder.unparsed("player", input));
            return;
        }

        Set<UUID> members = plugin.householdManager().membersOf(player.getUniqueId());
        String name = player.getName() == null ? input : player.getName();

        if (members.isEmpty()) {
            plugin.messageService().send(ctx.sender(), plugin.messages().householdNotLinked, Placeholder.unparsed("player", name)
            );

            return;
        }

        List<String> names = members.stream().map(this::displayPlayerName).sorted(String.CASE_INSENSITIVE_ORDER).toList();
        plugin.messageService().send(ctx.sender(), plugin.messages().householdInfo, Placeholder.unparsed("player", name), Placeholder.unparsed("members", String.join(", ", names))
        );
    }

    @VSub("afkarea household list")
    @VDesc("List all configured households")
    @VPerm("afkarea.admin.household")
    public void householdList(CommandContext ctx) {
        if (!plugin.householdManager().isLoaded()) {
            plugin.messageService().send(ctx.sender(), plugin.messages().householdDataLoading);
            return;
        }

        List<Set<UUID>> groups = plugin.householdManager().getMemberGroups();
        if (groups.isEmpty()) {
            plugin.messageService().send(ctx.sender(), plugin.messages().householdListEmpty);
            return;
        }

        List<String> entries = new ArrayList<>();
        for (Set<UUID> group : groups) {
            List<String> names = group.stream().map(this::displayPlayerName).sorted(String.CASE_INSENSITIVE_ORDER).toList();
            entries.add("- " + String.join(", ", names));
        }

        entries.sort(String.CASE_INSENSITIVE_ORDER);
        plugin.messageService().send(ctx.sender(), plugin.messages().householdList, Placeholder.unparsed("count", Integer.toString(groups.size())), Placeholder.unparsed("households", String.join("\n", entries))
        );
    }

    private OfflinePlayer findKnownPlayer(String name) {
        Player online = Bukkit.getPlayerExact(name);
        if(online != null) return online;

        for(OfflinePlayer offline : Bukkit.getOfflinePlayers()) {
            String knownName = offline.getName();

            if(knownName != null && knownName.equalsIgnoreCase(name)) return offline;
        }

        return null;
    }

    private String displayPlayerName(UUID playerId) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerId);
        String name = player.getName();

        return name == null ? playerId.toString() : name;
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

        List<String> types = new ArrayList<>();
        types.add("cuboid");

        if(plugin.worldGuardIntegration().isAvailable()) {
            types.add("worldguard");
        }

        return types.stream().filter(type -> type.startsWith(token)).toList();
    }

    @VSuggest("afkarea tp")
    public List<String> tpSuggest(CommandSender sender, String alias, String[] args) {
        return suggestAreaIds(sender, args, "afkarea.admin.tp");
    }

    @VSuggest("afkarea info")
    public List<String> infoSuggest(CommandSender sender, String alias, String[] args) {
        return suggestAreaIds(sender, args, "afkarea.admin.info");
    }

    @VSuggest("afkarea setregion")
    public List<String> setRegionSuggest(CommandSender sender, String alias, String[] args) {
        if(!sender.hasPermission("afkarea.admin.setregion")) return List.of();
        if(!plugin.worldGuardIntegration().isAvailable()) return List.of();
        if(args.length > 2) return List.of();

        String token = args.length >= 2 ? args[1] : "";
        String normalized = token.toLowerCase(Locale.ROOT);

        return plugin.areaManager().getAreas().stream().filter(area -> "worldguard".equalsIgnoreCase(area.getRegionType())).map(AreaData::getUniqueId).filter(id -> id != null && !id.isBlank()).filter(id -> id.toLowerCase(Locale.ROOT).startsWith(normalized)).sorted(String.CASE_INSENSITIVE_ORDER).toList();
    }
}
