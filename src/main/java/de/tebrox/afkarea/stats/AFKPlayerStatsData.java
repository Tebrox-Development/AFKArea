package de.tebrox.afkarea.stats;

import de.tebrox.afkarea.session.CompletedSession;
import de.tebrox.vertexCore.database.DataObject;
import de.tebrox.vertexCore.database.annotation.DbExpose;

import java.util.UUID;

public final class AFKPlayerStatsData implements DataObject {
    private String uniqueId;
    @DbExpose private long lastSessionSeconds;
    @DbExpose private long totalTimeSeconds;
    @DbExpose private long sessionCount;
    @DbExpose private long longestSessionSeconds;
    @DbExpose private String lastAreaId;
    @DbExpose private long lastSessionEndedAtEpochMillis;

    public AFKPlayerStatsData() {}

    public AFKPlayerStatsData(String uniqueId, long lastSessionSeconds, long totalTimeSeconds, long sessionCount, long longestSessionSeconds, String lastAreaId, long lastSessionEndedAtEpochMillis) {
        this.uniqueId = uniqueId;
        this.lastSessionSeconds = lastSessionSeconds;
        this.totalTimeSeconds = totalTimeSeconds;
        this.sessionCount = sessionCount;
        this.longestSessionSeconds = longestSessionSeconds;
        this.lastAreaId = lastAreaId;
        this.lastSessionEndedAtEpochMillis = lastSessionEndedAtEpochMillis;
    }

    public static AFKPlayerStatsData empty(UUID playerId) {
        return new AFKPlayerStatsData(playerId.toString(), 0L, 0L, 0L, 0L, null, 0L);
    }

    public void apply(CompletedSession session) {
        long duration = Math.max(0L, session.durationSeconds());

        lastSessionSeconds = duration;
        totalTimeSeconds = Math.max(0L, totalTimeSeconds) + duration;
        sessionCount = Math.max(0L, sessionCount) + 1L;
        longestSessionSeconds = Math.max(Math.max(0L, longestSessionSeconds), duration);

        lastAreaId = session.areaId();
        lastSessionEndedAtEpochMillis = Math.max(0L, session.endedAtEpochMillis());
    }

    public AFKPlayerStatsData copy() {
        return new AFKPlayerStatsData(uniqueId, lastSessionSeconds, totalTimeSeconds, sessionCount, longestSessionSeconds, lastAreaId, lastSessionEndedAtEpochMillis);
    }

    @Override
    public String getUniqueId() {
        return uniqueId;
    }

    @Override
    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public long getLastSessionSeconds() {
        return lastSessionSeconds;
    }

    public void setLastSessionSeconds(long lastSessionSeconds) {
        this.lastSessionSeconds = lastSessionSeconds;
    }

    public long getTotalTimeSeconds() {
        return totalTimeSeconds;
    }

    public void setTotalTimeSeconds(long totalTimeSeconds) {
        this.totalTimeSeconds = totalTimeSeconds;
    }

    public long getSessionCount() {
        return sessionCount;
    }

    public void setSessionCount(long sessionCount) {
        this.sessionCount = sessionCount;
    }

    public long getLongestSessionSeconds() {
        return longestSessionSeconds;
    }

    public void setLongestSessionSeconds(long longestSessionSeconds) {
        this.longestSessionSeconds = longestSessionSeconds;
    }

    public String getLastAreaId() {
        return lastAreaId;
    }

    public void setLastAreaId(String lastAreaId) {
        this.lastAreaId = lastAreaId;
    }

    public long getLastSessionEndedAtEpochMillis() {
        return lastSessionEndedAtEpochMillis;
    }

    public void setLastSessionEndedAtEpochMillis(long lastSessionEndedAtEpochMillis) {
        this.lastSessionEndedAtEpochMillis = lastSessionEndedAtEpochMillis;
    }
}