package de.tebrox.afkarea.area;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public final class AreaTeleportService {
    private final AreaManager areaManager;

    public AreaTeleportService(AreaManager areaManager) {
        this.areaManager = areaManager;
    }

    public Result teleport(Player player, String areaId, boolean requireEnabled) {
        AreaData area = areaManager.getArea(areaId);

        if(area == null) return Result.AREA_NOT_FOUND;
        if(requireEnabled && !area.isEnabled()) return Result.AREA_DISABLED;

        TeleportData teleport = area.getTeleport();
        if(teleport == null) return Result.TELEPORT_NOT_SET;

        World world = Bukkit.getWorld(teleport.getWorld());
        if(world == null) return Result.WORLD_UNAVAILABLE;

        Location target = new Location(world, teleport.getX(), teleport.getY(), teleport.getZ(), teleport.getYaw(), teleport.getPitch());
        return player.teleport(target) ? Result.SUCCESS : Result.TELEPORT_FAILED;
    }

    public enum Result {
        SUCCESS,
        AREA_NOT_FOUND,
        AREA_DISABLED,
        TELEPORT_NOT_SET,
        WORLD_UNAVAILABLE,
        TELEPORT_FAILED
    }
}
