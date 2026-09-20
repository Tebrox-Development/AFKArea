package de.tebrox.afkarea.display;

import de.tebrox.afkarea.reward.RewardSessionService;
import de.tebrox.afkarea.util.DurationFormatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.Optional;
import java.util.OptionalLong;
import java.util.UUID;

public record AFKDisplayContext(long sessionSeconds, Optional<RewardSessionService.RewardProgress> rewardProgress
) {

    public static Optional<AFKDisplayContext> resolve(RewardSessionService rewardSessionService, UUID playerId
    ) {
        OptionalLong session = rewardSessionService.getSessionSeconds(playerId);
        if(session.isEmpty()) return Optional.empty();

        return Optional.of(new AFKDisplayContext(session.getAsLong(), rewardSessionService.getRewardProgress(playerId)));
    }

    public String sessionText() {
        return DurationFormatter.formatDuration(sessionSeconds);
    }

    public String nextRewardText() {
        return rewardProgress.map(progress -> DurationFormatter.formatDuration(progress.remainingSeconds())).orElse("-");
    }

    public String progressPercentText() {
        return rewardProgress.map(progress -> Long.toString(Math.round(progress.progress() * 100.0D))).orElse("0");
    }

    public float progress() {
        return rewardProgress.map(value -> (float) Math.max(0.0D, Math.min(1.0D, value.progress()))).orElse(0.0F);
    }

    public boolean hasNextReward() {
        return rewardProgress.isPresent();
    }

    public TagResolver[] placeholders() {
        return new TagResolver[] {
                Placeholder.unparsed("session", sessionText()),
                Placeholder.unparsed("next_reward", nextRewardText()),
                Placeholder.unparsed("progress_percent", progressPercentText())
        };
    }
}