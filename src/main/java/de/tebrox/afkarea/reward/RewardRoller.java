package de.tebrox.afkarea.reward;

import de.tebrox.afkarea.reward.data.CommandRewardData;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public final class RewardRoller {
    private final RewardPool rewardPool;

    public RewardRoller() {
        this(new RewardPool());
    }

    public RewardRoller(RewardPool rewardPool) {
        this.rewardPool = rewardPool;
    }

    public List<CommandRewardData> roll(List<CommandRewardData> rewards, int rolls, boolean allowDuplicates) {
        return roll(rewards, rolls, allowDuplicates, reward -> true);
    }

    public List<CommandRewardData> roll(List<CommandRewardData> rewards, int rolls, boolean allowDuplicates, Predicate<CommandRewardData> eligibility) {
        if(rewards == null || rewards.isEmpty() || rolls <= 0) return List.of();

        List<CommandRewardData> available = new ArrayList<>(rewards);
        List<CommandRewardData> selected = new ArrayList<>();

        for(int i = 0; i < rolls; i++) {
            List<CommandRewardData> source = allowDuplicates ? rewards : available;
            var reward = rewardPool.select(source, eligibility);

            if(reward.isEmpty()) break;

            CommandRewardData winner = reward.get();
            selected.add(winner);

            if(!allowDuplicates) available.remove(winner);
        }

        return List.copyOf(selected);
    }
}
