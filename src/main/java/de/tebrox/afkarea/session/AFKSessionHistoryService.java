package de.tebrox.afkarea.session;

import de.tebrox.afkarea.area.AreaEntrySource;
import de.tebrox.afkarea.reward.RewardSessionService;
import de.tebrox.vertexCore.database.Database;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public final class AFKSessionHistoryService {
    private final JavaPlugin plugin;
    private final Database<AFKSessionData> database;

    public AFKSessionHistoryService(JavaPlugin plugin, Database<AFKSessionData> database) {
        this.plugin = plugin;
        this.database = database;
    }
    public void save(Player player, CompletedSession session, AreaEntrySource entrySource) {
        String playerName = player.getName();

        AFKSessionData data = new AFKSessionData(UUID.randomUUID().toString(), player.getUniqueId().toString(), session.areaId(), session.durationSeconds(), session.startedAtEpochMillis(), session.endedAtEpochMillis(), entrySource == null ? "UNKNOW" : entrySource.name());
        database.saveObjectAsyncMain(data, () -> {}, error -> {
            plugin.getLogger().severe("Failed to persist completed AFK area session for player '" + playerName + "': " + error.getMessage());
            error.printStackTrace();
        });
    }
}
