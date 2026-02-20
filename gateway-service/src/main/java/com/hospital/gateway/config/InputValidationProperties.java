package com.hospital.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Configuration for strict input validation (US1.6).
 * Excluded paths skip injection pattern checks (query/headers).
 */
@Component
@ConfigurationProperties(prefix = "input-validation")
public class InputValidationProperties {

    /** Whether input validation filter is enabled (default true). */
    private boolean enabled = true;

    /** Paths that skip injection detection (e.g. /actuator/health). */
    private List<String> excludedPaths = List.of("/actuator/health");

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getExcludedPaths() {
        return excludedPaths;
    }

    public void setExcludedPaths(List<String> excludedPaths) {
        this.excludedPaths = excludedPaths != null ? excludedPaths : List.of();
    }
}
