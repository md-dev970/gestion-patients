package com.hospital.gateway.bruteforce;

import com.hospital.gateway.config.BruteforceByIpProperties;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory store for anti-bruteforce by IP (T1.6): counter per IP, TTL block after N failures.
 */
@Component
public class BruteforceByIpStore {

    private final BruteforceByIpProperties properties;
    private final ConcurrentHashMap<String, Slot> slots = new ConcurrentHashMap<>();

    public BruteforceByIpStore(BruteforceByIpProperties properties) {
        this.properties = properties;
    }

    /**
     * Returns true if this IP is currently blocked (within lockout TTL).
     */
    public boolean isBlocked(String ip) {
        if (ip == null || ip.isBlank()) return false;
        Slot slot = slots.get(ip);
        if (slot == null) return false;
        long blockUntil = slot.blockUntilEpochMs.get();
        if (blockUntil == 0) return false;
        long now = System.currentTimeMillis();
        if (now < blockUntil) return true;
        // Expired: clear block so they can try again
        slots.remove(ip);
        return false;
    }

    /**
     * Records a failed login (401/423) from this IP. Call only when the downstream returned 401 or 423.
     *
     * @return true if this failure caused the IP to become blocked (count reached N)
     */
    public boolean recordFailure(String ip) {
        if (ip == null || ip.isBlank()) return false;
        long now = System.currentTimeMillis();
        long ttlMs = properties.getLockoutDurationMinutes() * 60L * 1000L;
        int maxAttempts = properties.getMaxFailedAttempts();

        Slot slot = slots.compute(ip, (k, existing) -> existing == null ? new Slot() : existing);
        int count = slot.count.incrementAndGet();
        if (count >= maxAttempts) {
            slot.blockUntilEpochMs.set(now + ttlMs);
            return true;
        }
        return false;
    }

    private static final class Slot {
        private final AtomicInteger count = new AtomicInteger(0);
        private final AtomicLong blockUntilEpochMs = new AtomicLong(0);
    }
}
