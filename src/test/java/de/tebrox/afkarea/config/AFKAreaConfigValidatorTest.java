package de.tebrox.afkarea.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AFKAreaConfigValidatorTest {

    @Test
    void acceptsDefaultConfiguration() {
        ConfigValidationResult result = AFKAreaConfigValidator.validate(new AFKAreaConfig());
        assertTrue(result.isValid());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void rejectsTeleportTimeoutBelowAfkTimeout() {
        AFKAreaConfig config = new AFKAreaConfig();
        config.markAfterSeconds = 300;
        config.teleportAfterSeconds = 120;

        ConfigValidationResult result = AFKAreaConfigValidator.validate(config);

        assertFalse(result.isValid());
        assertTrue(result.errors().contains("afk.teleport-after-seconds must not be lower than afk.mark-after-seconds."));
    }

    @Test
    void rejectsZeroRewardingPlayerPerIp() {
        AFKAreaConfig config = new AFKAreaConfig();
        config.maxRewardingPlayersPerIp = 0;
        ConfigValidationResult result = AFKAreaConfigValidator.validate(config);
        assertFalse(result.isValid());
        assertTrue(result.errors().stream().anyMatch(error -> error.contains("max-rewarding-players-per-ip")));
    }
}
