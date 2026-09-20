package de.tebrox.afkarea.reward;

import de.tebrox.afkarea.reward.data.CommandRewardData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RewardRollerTest {

    private final RewardRoller roller = new RewardRoller();

    @Test
    void stopsWhenNoDuplicateRewardsRemain() {
        CommandRewardData first = reward("first");
        CommandRewardData second = reward("seconds");
        List<CommandRewardData> result = roller.roll(List.of(first, second), 5, false);

        assertEquals(2, result.size());
        assertTrue(result.contains(first));
        assertTrue(result.contains(second));
    }

    @Test
    void allowsRepeatedRewardWhenDuplicatesAreEnabled() {
        CommandRewardData reward = reward("diamond");

        List<CommandRewardData> result = roller.roll(List.of(reward), 3, true);
        assertEquals(List.of(reward, reward, reward), result);
    }

    @Test
    void respectsEligibilityPredicate() {
        CommandRewardData blocked = reward("blocked");
        CommandRewardData allowed = reward("allowed");

        List<CommandRewardData> result = roller.roll(List.of(blocked, allowed), 1, false, reward -> reward == allowed);
        assertEquals(List.of(allowed), result);
    }

    private CommandRewardData reward(String id) {
        CommandRewardData reward = new CommandRewardData(id);

        reward.setEnabled(true);
        reward.setWeight(1.0D);
        reward.setCommands(List.of("say test"));

        return reward;
    }
}
