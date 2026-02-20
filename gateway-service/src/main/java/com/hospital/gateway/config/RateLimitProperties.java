package com.hospital.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Configuration for rate limiting (per IP and per user).
 */
@Component
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitProperties {

    /** Max requests per window per IP (default 100). */
    private int requestsPerMinutePerIp = 100;

    /** Max requests per window per authenticated user (default 100). */
    private int requestsPerMinutePerUser = 100;

    /** Window duration in seconds (default 60). */
    private int windowSeconds = 60;

    /** Paths that do not consume rate limit quota (e.g. /actuator/health). */
    private List<String> excludedPaths = List.of("/actuator/health");

    public int getRequestsPerMinutePerIp() {
        return requestsPerMinutePerIp;
    }

    public void setRequestsPerMinutePerIp(int requestsPerMinutePerIp) {
        this.requestsPerMinutePerIp = requestsPerMinutePerIp;
    }

    public int getRequestsPerMinutePerUser() {
        return requestsPerMinutePerUser;
    }

    public void setRequestsPerMinutePerUser(int requestsPerMinutePerUser) {
        this.requestsPerMinutePerUser = requestsPerMinutePerUser;
    }

    public int getWindowSeconds() {
        return windowSeconds;
    }

    public void setWindowSeconds(int windowSeconds) {
        this.windowSeconds = windowSeconds;
    }

    public List<String> getExcludedPaths() {
        return excludedPaths;
    }

    public void setExcludedPaths(List<String> excludedPaths) {
        this.excludedPaths = excludedPaths != null ? excludedPaths : List.of();
    }
}
