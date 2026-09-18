package de.tebrox.afkarea.stats;

import de.tebrox.afkarea.session.CompletedSession;
import de.tebrox.vertexCore.database.Database;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class AFKPlayerStatsService {
    private final JavaPlugin plugin;
    private final Database<AFKPlayerStatsData> database;

    private final Map<UUID, AFKPlayerStatsData> stats = new HashMap<>();

    private final Map<UUID, List<CompletedSession>> pendingSessions = new HashMap<>();

    private boolean loaded;

    public AFKPlayerStatsService(JavaPlugin plugin, Database<AFKPlayerStatsData> database) {
        this.plugin = plugin;
        this.database = database;
    }

    public void loadAsync() {
        loaded = false;

        database.loadObjectsAsyncMain(this::replaceCache, error -> {
            plugin.getLogger().severe("Failed to load AFK player statistics: " + error.getMessage());
            error.printStackTrace();
            }
        );
    }

    private void replaceCache(Collection<AFKPlayerStatsData> loadedStats) {
        stats.clear();

        for(AFKPlayerStatsData raw : loadedStats) {
            String rawId = raw.getUniqueId();

            if(rawId == null || rawId.isBlank()) {
                plugin.getLogger().warning("Skipping AFK player statistics with missing player UUID");
                continue;
            }

            UUID playerId;

            try {
                playerId = UUID.fromString(rawId);
            }catch(IllegalArgumentException exception) {
                plugin.getLogger().warning("Skipping AFK player statistics with invalid player UUID '" + rawId + "'");
                continue;
            }

            AFKPlayerStatsData normalized = raw.copy();
            normalized.setUniqueId(playerId.toString());

            stats.put(playerId, normalized);
        }
        loaded = true;

        Map<UUID, List<CompletedSession>> queued = new HashMap<>(pendingSessions);

        pendingSessions.clear();

        for(Map.Entry<UUID, List<CompletedSession>> entry : queued.entrySet()) {
            UUID playerId = entry.getKey();

            for(CompletedSession session : entry.getValue()) {
                applyAndSave(playerId, session);
            }
        }

        plugin.getLogger().info("Loaded " + stats.size() + " AFK player statistic" + (stats.size() == 1 ? "" : "s"));
    }

    public void record(UUID playerId, CompletedSession session) {
        if(playerId == null || session == null) return;

        if(!loaded) {
            pendingSessions.computeIfAbsent(playerId, ignored -> new ArrayList<>()).add(session);
            return;
        }

        applyAndSave(playerId, session);
    }

    private void applyAndSave(UUID playerId, CompletedSession session) {
        AFKPlayerStatsData current = stats.get(playerId);
        AFKPlayerStatsData updated = current == null ? AFKPlayerStatsData.empty(playerId) : current.copy();
        updated.apply(session);

        stats.put(playerId, updated);

        database.saveObjectAsyncMain(
                updated,
                () -> {},
                error -> {
                    plugin.getLogger().severe("Failed to persist AFK player statistics for '" + playerId + "': " + error.getMessage());

                    error.printStackTrace();
                }
        );
    }

    public Optional<AFKPlayerStatsData> getStats(UUID playerId) {
        if(!loaded || playerId == null) return Optional.empty();
        AFKPlayerStatsData data = stats.get(playerId);

        return data == null ? Optional.empty() : Optional.of(data.copy());
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void clear() {
        stats.clear();
        pendingSessions.clear();
        loaded = false;
    }
}