package de.tebrox.afkarea.reward;

import de.tebrox.afkarea.reward.data.CommandRewardData;
import de.tebrox.afkarea.reward.data.RewardConfigData;
import de.tebrox.afkarea.reward.data.RewardMilestoneData;

import java.util.ArrayList;
import java.util.List;

public final class RewardConfigValidator {
    private RewardConfigValidator() {}

    public static List<String> validate(RewardConfigData config) {
        if(config == null) return List.of();

        List<String> warnings = new ArrayList<>();
        String type = config.getScheduleType();

        if(!"interval".equalsIgnoreCase(type) && !"milestones".equalsIgnoreCase(type)) {
            warnings.add("unsupported reward schedule type '" + type + "'");
            return warnings;
        }

        if("interval".equalsIgnoreCase(type)) {
            if(config.getIntervalSeconds() <= 0) warnings.add("reward interval must be greater than 0 seconds.");
            if(config.getRolls() <= 0) warnings.add("reward rolls must be greater than 0.");
        }

        if("milestones".equalsIgnoreCase(type)) {
            List<RewardMilestoneData> milestones = config.getMilestones();

            if(milestones == null || milestones.isEmpty()) warnings.add("milestones schedule contains no milestones.");
            else{
                for(int index = 0; index < milestones.size(); index++) {
                    RewardMilestoneData milestone = milestones.get(index);

                    if(milestone == null) {
                        warnings.add("milestone #" + index + " is null");
                        continue;
                    }

                    if(milestone.getAfterSeconds() <= 0) warnings.add("milestone #" + index + " must have afterSeconds greater than 0.");
                    if(milestone.getRolls() <= 0) warnings.add("milestone #" + index + " must have rolls greater than 0.");
                }
            }
        }

        List<CommandRewardData> rewards = config.getRewards();
        if(rewards == null || rewards.isEmpty()) {
            warnings.add("reward pool contains no rewards.");
            return warnings;
        }

        for(CommandRewardData reward : rewards) {
            if(reward == null || !reward.isEnabled()) continue;

            String id = reward.getId();
            if(id == null || id.isBlank()) warnings.add("an enabled reward has no ID.");
            if(!Double.isFinite(reward.getWeight()) || reward.getWeight() <= 0.0) warnings.add("reward '" + displayId(reward) + "' has an invalid weight and will not be selected.");
            if(reward.getCommands() == null || reward.getCommands().stream().noneMatch(command -> command != null && !command.isBlank())) warnings.add("reward '" + displayId(reward) + "' contains no executable commands.");
        }

        return warnings;
    }

    private static String displayId(CommandRewardData reward) {
        String id = reward.getId();
        return id == null || id.isBlank() ? "<unnamed>" : id;
    }
}
