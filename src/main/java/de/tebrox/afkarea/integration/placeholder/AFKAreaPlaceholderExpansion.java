package de.tebrox.afkarea.integration.placeholder;

import de.tebrox.afkarea.area.AreaData;
import de.tebrox.afkarea.bootstrap.AFKAreaPlugin;
import de.tebrox.afkarea.state.PlayerState;
import de.tebrox.afkarea.stats.AFKPlayerStatsData;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.OptionalLong;
import java.util.UUID;

public final class AFKAreaPlaceholderExpansion extends PlaceholderExpansion {
    private final AFKAreaPlugin plugin;

    public AFKAreaPlaceholderExpansion(AFKAreaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "afkarea";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getPluginMeta().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        if(offlinePlayer == null) return null;

        UUID playerId = offlinePlayer.getUniqueId();
        PlayerState state = plugin.playerStateService().getState(playerId);
        Optional<AFKPlayerStatsData> stats = plugin.playerStatsService().getStats(playerId);


        switch (params) {
            case "state": return state.name().toLowerCase();
            case "is_afk": return Boolean.toString(plugin.playerStateService().isAfk(playerId));
            case "idle_seconds": return Long.toString(plugin.activityService().getIdleDuration(playerId).toSeconds());
            case "area_id": {
                String areaId = plugin.areaSessionService().getAreaId(playerId);
                return areaId == null ? "" : areaId;
            }
            case "area_name": {
                String areaId  = plugin.areaSessionService().getAreaId(playerId);
                if(areaId == null) return "";

                AreaData area = plugin.areaManager().getArea(areaId);
                if(area == null) return "";

                String name = area.getName();
                return name == null || name.isBlank() ? area.getUniqueId() : name;
            }
            case "session_seconds": {
                OptionalLong duration = plugin.rewardSessionService().getSessionSeconds(playerId);
                return duration.isPresent() ? Long.toString(duration.getAsLong()) : "0";
            }
            case "stats_session_count": return stats.map(data -> Long.toString(data.getSessionCount())).orElse("0");
            case "stats_total_seconds": return stats.map(data -> Long.toString(data.getTotalTimeSeconds())).orElse("0");
            case "stats_last_session_seconds": return stats.map(data -> Long.toString(data.getLastSessionSeconds())).orElse("0");
            case "stats_longest_session_seconds": return stats.map(data -> Long.toString(data.getLongestSessionSeconds())).orElse("0");
            case "stats_last_area_id": return stats.map(AFKPlayerStatsData::getLastAreaId).orElse("");
            case "stats_last_session_end_epoch_millis": return stats.map(data -> Long.toString(data.getLastSessionEndedAtEpochMillis())).orElse("0");

            default: return null;
        }
    }
}
