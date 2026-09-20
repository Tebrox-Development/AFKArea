package de.tebrox.afkarea.reward.data;

public final class RewardMilestoneData {
    private int afterSeconds;
    private int rolls = 1;

    public RewardMilestoneData() {}

    public RewardMilestoneData(int afterSeconds, int rolls) {
        this.afterSeconds = afterSeconds;
        this.rolls = rolls;
    }

    public int getAfterSeconds() {
        return afterSeconds;
    }

    public void setAfterSeconds(int afterSeconds) {
        this.afterSeconds = afterSeconds;
    }

    public int getRolls() {
        return rolls;
    }

    public void setRolls(int rolls) {
        this.rolls = rolls;
    }

    public RewardMilestoneData copy() {
        return new RewardMilestoneData(afterSeconds, rolls);
    }
}
