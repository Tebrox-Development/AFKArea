package de.tebrox.afkarea.area.selection;

public record CuboidSelection(SelectionPoint pos1, SelectionPoint pos2) {
    public boolean isComplete() {
        return pos1 != null && pos2 != null;
    }

    public boolean isSameWorld() {
        return isComplete() && pos1.world().equals(pos2.world());
    }
}
