package de.tebrox.afkarea.util;

import java.util.Locale;

public final class DurationFormatter {
    private DurationFormatter() {}

    public static String formatDuration(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        return String.format(Locale.ROOT, "%02d:%02d:%02d", hours, minutes, secs);
    }
}
