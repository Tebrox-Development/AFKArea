package de.tebrox.afkarea.session;

import de.tebrox.vertexCore.database.DataObject;
import de.tebrox.vertexCore.database.annotation.DbExpose;

public final class AFKSessionData implements DataObject {
    private String uniqueId;
    @DbExpose private String playerId;

    @DbExpose private String areaId;
    @DbExpose private long durationSeconds;
    @DbExpose private long startedAtEpochMillis;
    @DbExpose private long endedAtEpochMillis;
    @DbExpose private String entrySource;

    public AFKSessionData() {}

    public AFKSessionData(String uniqueId, String playerId, String areaId, long durationSeconds, long startedAtEpochMillis, long endedAtEpochMillis, String entrySource) {
        this.uniqueId = uniqueId;
        this.playerId = playerId;
        this.areaId = areaId;
        this.durationSeconds = durationSeconds;
        this.startedAtEpochMillis = startedAtEpochMillis;
        this.endedAtEpochMillis = endedAtEpochMillis;
        this.entrySource = entrySource;
    }

    @Override
    public String getUniqueId() {
        return uniqueId;
    }

    @Override
    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getAreaId() {
        return areaId;
    }

    public void setAreaId(String areaId) {
        this.areaId = areaId;
    }

    public long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public long getStartedAtEpochMillis() {
        return startedAtEpochMillis;
    }

    public void setStartedAtEpochMillis(long startedAtEpochMillis) {
        this.startedAtEpochMillis = startedAtEpochMillis;
    }

    public String getEntrySource() {
        return entrySource;
    }

    public void setEntrySource(String entrySource) {
        this.entrySource = entrySource;
    }
}
