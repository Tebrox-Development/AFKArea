package de.tebrox.afkarea.state;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PlayerStateService {
    private final Map<UUID, PlayerState> states = new HashMap<>();

    public PlayerState getState(UUID playerId) {
        return states.getOrDefault(playerId, PlayerState.ACTIVE);
    }

    public void setState(UUID playerId, PlayerState state) {
        if(state == PlayerState.ACTIVE) {
            states.remove(playerId);
            return;
        }

        states.put(playerId, state);
    }

    public boolean isAfk(UUID playerId) {
        return getState(playerId) != PlayerState.ACTIVE;
    }

    public void clear(UUID playerId) {
        states.remove(playerId);
    }

    public void clearAll() {
        states.clear();
    }
}
