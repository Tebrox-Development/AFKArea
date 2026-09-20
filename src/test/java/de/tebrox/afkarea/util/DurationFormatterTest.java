package de.tebrox.afkarea.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DurationFormatterTest {

    @Test
    void formatsZero() {
        assertEquals("00:00:00", DurationFormatter.formatDuration(0));
    }

    @Test
    void formatsMinutesAndSeconds() {
        assertEquals("00:02:05", DurationFormatter.formatDuration(125));
    }

    @Test
    void formatsHour() {
        assertEquals("01:01:01", DurationFormatter.formatDuration(3661));
    }
}
