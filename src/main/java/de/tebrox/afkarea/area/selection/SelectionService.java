package de.tebrox.afkarea.area.selection;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class SelectionService {
    private final Map<UUID, CuboidSelection> selections = new HashMap<>();

    public void setPos1(UUID playerId, SelectionPoint point) {
        selections.compute(playerId, (k, current) -> new CuboidSelection(point, current == null ? null : current.pos2()));
    }

    public void setPos2(UUID playerId, SelectionPoint point) {
        selections.compute(playerId, (k, current) -> new CuboidSelection(point, current == null ? null : current.pos1()));
    }

    public CuboidSelection getSelection(UUID playerId) {
        return selections.get(playerId);
    }

    public void clear(UUID playerId) {
        selections.remove(playerId);
    }

    public void clearAll() {
        selections.clear();
    }
}
