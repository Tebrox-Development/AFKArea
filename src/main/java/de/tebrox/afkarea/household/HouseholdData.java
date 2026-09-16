package de.tebrox.afkarea.household;

import de.tebrox.vertexCore.database.DataObject;
import de.tebrox.vertexCore.database.annotation.DbExpose;

import java.util.ArrayList;
import java.util.List;

public final class HouseholdData implements DataObject {

    private String uniqueId;
    @DbExpose private List<String> members = new ArrayList<>();

    public HouseholdData() {}

    public HouseholdData(String uniqueId, List<String> members) {
        this.uniqueId = uniqueId;
        setMembers(members);
    }

    @Override
    public String getUniqueId() {
        return uniqueId;
    }

    @Override
    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members == null ? new ArrayList<>() : new ArrayList<>(members);
    }

    public HouseholdData copy() {
        return new HouseholdData(uniqueId, members);
    }
}