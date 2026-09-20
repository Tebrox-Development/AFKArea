package de.tebrox.afkarea.command.suggestion;

import de.tebrox.afkarea.area.AreaData;
import de.tebrox.afkarea.bootstrap.AFKAreaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public final class AFKAreaCommandSuggestions {
    private final AFKAreaPlugin plugin;

    public AFKAreaCommandSuggestions(AFKAreaPlugin plugin) {
        this.plugin = plugin;
    }

    public List<String> areaIds(CommandSender sender, String[] args, String permission) {
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

    public List<String> createTypes(CommandSender sender, String[] args, String permission) {
        if(!sender.hasPermission(permission)) {
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

    public List<String> worldGuardAreaIds(CommandSender sender, String[] args, String permission) {
        if(!sender.hasPermission(permission)) return List.of();
        if(!plugin.worldGuardIntegration().isAvailable()) return List.of();
        if(args.length > 2) return List.of();

        String token = args.length >= 2 ? args[1] : "";
        String normalized = token.toLowerCase(Locale.ROOT);

        return plugin.areaManager().getAreas().stream().filter(area -> "worldguard".equalsIgnoreCase(area.getRegionType())).map(AreaData::getUniqueId).filter(id -> id != null && !id.isBlank()).filter(id -> id.toLowerCase(Locale.ROOT).startsWith(normalized)).sorted(String.CASE_INSENSITIVE_ORDER).toList();
    }

    public List<String> onlinePlayers(CommandSender sender, String[] args, String permission) {
        if (!sender.hasPermission(permission)) return List.of();
        if (args.length > 2) return List.of();

        String token = args.length >= 2 ? args[1].toLowerCase(Locale.ROOT) : "";

        return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(name -> name.toLowerCase(Locale.ROOT).startsWith(token)).sorted(String.CASE_INSENSITIVE_ORDER).toList();
    }

    public List<String> knownPlayers(CommandSender sender, String[] args, String permission) {
        if(!sender.hasPermission(permission)) return List.of();
        if(args.length > 2) return List.of();

        String token = args.length >= 2 ? args[1].toLowerCase(Locale.ROOT) : "";

        return Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName).filter(Objects::nonNull).filter(name -> name.toLowerCase(Locale.ROOT).startsWith(token)).sorted(String.CASE_INSENSITIVE_ORDER).toList();
    }
}
