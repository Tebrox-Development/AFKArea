package de.tebrox.afkarea.reward.data;

import java.util.ArrayList;
import java.util.List;

public final class CommandRewardData {
    private String id;
    private boolean enabled = true;
    private double weight = 1.0;
    private String permission;
    private List<String> commands = new ArrayList<>();
    private String message;

    public CommandRewardData() {}

    public CommandRewardData(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public List<String> getCommands() {
        return commands;
    }

    public void setCommands(List<String> commands) {
        this.commands = commands == null ? new ArrayList<>() : new ArrayList<>(commands);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public CommandRewardData copy() {
        CommandRewardData copy = new CommandRewardData(id);

        copy.enabled = enabled;
        copy.weight = weight;
        copy.permission = permission;
        copy.commands = commands == null ? new ArrayList<>() : new ArrayList<>(commands);
        copy.message = message;

        return copy;
    }
}
