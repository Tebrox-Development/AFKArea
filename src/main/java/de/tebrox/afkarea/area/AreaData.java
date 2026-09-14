package de.tebrox.afkarea.area;

import de.tebrox.vertexCore.database.DataObject;

public final class AreaData implements DataObject {
    private String uniqueId;
    private String name;
    private boolean enabled = true;
    private int priority = 0;

    public AreaData() {}

    public AreaData(String uniqueId, String name) {
        this.uniqueId = uniqueId;
        this.name = name;
    }

    @Override
    public String getUniqueId() {
        return uniqueId;
    }

    @Override
    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
