package com.hospital.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration for anti-bruteforce protection on login.
 */
@Component
@ConfigurationProperties(prefix = "auth.bruteforce")
public class BruteforceProperties {

    /** Max failed login attempts before temporary lock (default 5). */
    private int maxFailedAttempts = 5;

    /** Lockout duration in minutes (default 15). */
    private int lockoutDurationMinutes = 15;

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
}
