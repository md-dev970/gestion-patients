package com.hospital.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration for anti-bruteforce by IP (T1.6): counter per IP, TTL block after N failures.
 */
@Component
@ConfigurationProperties(prefix = "bruteforce-ip")
public class BruteforceByIpProperties {

    /** Max failed login attempts (401/423) from same IP before block (default 5). */
    private int maxFailedAttempts = 5;

    /** Block duration in minutes (default 15). */
    private int lockoutDurationMinutes = 15;

    /** Path that triggers counting (only failures on this path count; default login). */
    private String loginPath = "/api/auth/login";

    public int getMaxFailedAttempts() {
        return maxFailedAttempts;
    }

    public void setMaxFailedAttempts(int maxFailedAttempts) {
        this.maxFailedAttempts = maxFailedAttempts;
    }

    public int getLockoutDurationMinutes() {
        return lockoutDurationMinutes;
    }

    public void setLockoutDurationMinutes(int lockoutDurationMinutes) {
        this.lockoutDurationMinutes = lockoutDurationMinutes;
    }

    public String getLoginPath() {
        return loginPath;
    }

    public void setLoginPath(String loginPath) {
        this.loginPath = loginPath != null ? loginPath : "/api/auth/login";
    }
}
