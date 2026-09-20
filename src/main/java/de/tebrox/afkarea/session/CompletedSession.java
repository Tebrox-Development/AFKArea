package de.tebrox.afkarea.session;

public record CompletedSession(String areaId, long durationSeconds, long startedAtEpochMillis, long endedAtEpochMillis) {}
