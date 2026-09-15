package de.tebrox.afkarea.reward.data;

import java.util.ArrayList;
import java.util.List;

public final class RewardConfigData {
    private String scheduleType = "interval";
    private int intervalSeconds = 300;
    private int rolls = 1;
    private boolean allowDuplicates;

    private List<CommandRewardData> rewards = new ArrayList<>();
    private List<RewardMilestoneData> milestones = new ArrayList<>();

    public String getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(String scheduleType) {
        this.scheduleType = scheduleType;
    }

    public int getIntervalSeconds() {
        return intervalSeconds;
    }

    public void setIntervalSeconds(int intervalSeconds) {
        this.intervalSeconds = intervalSeconds;
    }

    public int getRolls() {
        return rolls;
    }

    public void setRolls(int rolls) {
        this.rolls = rolls;
    }

    public boolean isAllowDuplicates() {
        return allowDuplicates;
    }

    public void setAllowDuplicates(boolean allowDuplicates) {
        this.allowDuplicates = allowDuplicates;
    }

    public List<CommandRewardData> getRewards() {
        return rewards;
    }

    public void setRewards(List<CommandRewardData> rewards) {
        this.rewards = rewards == null ? new ArrayList<>() : new ArrayList<>(rewards);
    }

    public List<RewardMilestoneData> getMilestones() {
        return milestones;
    }

    public void setMilestones(List<RewardMilestoneData> milestones) {
        this.milestones = milestones == null ? new ArrayList<>() : new ArrayList<>(milestones);
    }

    public RewardConfigData copy() {
        RewardConfigData copy = new RewardConfigData();

        copy.scheduleType = scheduleType;
        copy.intervalSeconds = intervalSeconds;
        copy.rolls = rolls;
        copy.allowDuplicates = allowDuplicates;

        copy.rewards = new ArrayList<>();

        for (CommandRewardData reward : rewards) {
            copy.rewards.add(reward.copy());
        }

        copy.milestones = new ArrayList<>();

        for (RewardMilestoneData milestone : milestones) {
            copy.milestones.add(milestone.copy());
        }

        return copy;
    }
}
