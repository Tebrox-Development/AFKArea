package de.tebrox.afkarea.command.support;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class KnownPlayerLookup {
    private KnownPlayerLookup() {}

    public static OfflinePlayer find(String name) {
        Player online = Bukkit.getPlayerExact(name);
        if(online != null) return online;

        for(OfflinePlayer offline : Bukkit.getOfflinePlayers()) {
            String knownName = offline.getName();

            if(knownName != null && knownName.equalsIgnoreCase(name)) return offline;
        }

        return null;
    }

    public static String displayPlayerName(UUID playerId) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerId);
        String name = player.getName();

        return name == null ? playerId.toString() : name;
    }
}
