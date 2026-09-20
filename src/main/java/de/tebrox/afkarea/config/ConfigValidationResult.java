package de.tebrox.afkarea.config;

import java.util.List;

public record ConfigValidationResult(List<String> errors, List<String> warnings) {
    public ConfigValidationResult {
        errors = List.copyOf(errors);
        warnings = List.copyOf(warnings);
    }

    public boolean isValid() {
        return errors.isEmpty();
    }
}