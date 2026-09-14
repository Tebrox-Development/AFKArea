package de.tebrox.afkarea.activity;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ActivityService {
    private final Map<UUID, Long> lastActivity = new HashMap<>();

    public void track(UUID playerId) {
        lastActivity.putIfAbsent(playerId, System.currentTimeMillis());
    }

    public void recordActivity(UUID playerId) {
        lastActivity.put(playerId, System.currentTimeMillis());
    }

    public Duration getIdleDuration(UUID playerId) {
        Long last = lastActivity.get(playerId);

        if(last == null) return Duration.ZERO;

        return Duration.ofMillis(Math.max(0, System.currentTimeMillis() - last));
    }

    public void clear(UUID playerId) {
        lastActivity.remove(playerId);
    }

    public void clearAll() {
        lastActivity.clear();
    }
}
