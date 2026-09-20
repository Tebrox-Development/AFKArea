package de.tebrox.afkarea.reward;

import de.tebrox.afkarea.reward.data.CommandRewardData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;

public final class RewardPool {
    private final RandomGenerator random;

    public RewardPool() {
        this(new Random());
    }

    public RewardPool(RandomGenerator random) {
        this.random = random;
    }

    public Optional<CommandRewardData> select(List<CommandRewardData> rewards) {
        return select(rewards, reward -> true);
    }

    public Optional<CommandRewardData> select(List<CommandRewardData> rewards, Predicate<CommandRewardData> eligibility) {
        if(rewards == null || rewards.isEmpty()) return Optional.empty();

        List<CommandRewardData> candidates = new ArrayList<>();
        double totalWeight = 0.0;

        for(CommandRewardData reward : rewards) {
            if(reward == null) continue;
            if(!reward.isEnabled()) continue;
            if(!eligibility.test(reward)) continue;

            double weight = reward.getWeight();

            if(!Double.isFinite(weight) || weight <= 0.0) continue;

            candidates.add(reward);
            totalWeight += weight;
        }

        if(candidates.isEmpty() || !Double.isFinite(totalWeight) || totalWeight <= 0.0) return Optional.empty();

        double roll = random.nextDouble(totalWeight);
        double cumulative = 0.0;

        for(CommandRewardData reward : candidates) {
            cumulative += reward.getWeight();

            if(roll < cumulative) return Optional.of(reward);
        }

        return Optional.of(candidates.get(candidates.size() - 1));
    }
}
