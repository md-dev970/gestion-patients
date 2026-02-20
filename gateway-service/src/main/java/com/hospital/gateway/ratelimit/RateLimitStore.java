package com.hospital.gateway.ratelimit;

import com.hospital.gateway.config.RateLimitProperties;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate limit store (T1.5): in-memory, thread-safe. Uses a fixed window per key.
 * Expired windows are replaced on access to avoid unbounded growth; map grows with distinct keys (IPs, user IDs).
 * Redis can be added later for distributed rate limiting.
 */
@Component
public class RateLimitStore {

    private final RateLimitProperties properties;
    private final ConcurrentHashMap<String, WindowSlot> slots = new ConcurrentHashMap<>();

    public RateLimitStore(RateLimitProperties properties) {
        this.properties = properties;
    }

    /**
     * Tries to consume one request for the given key within the configured window and limit.
     *
     * @param key            e.g. "ip:192.168.1.1" or "user:42"
     * @param limitPerWindow max requests allowed in one window for this key
     * @return true if the request is allowed, false if limit exceeded
     */
    public boolean tryConsume(String key, int limitPerWindow) {
        long nowMs = System.currentTimeMillis();
        long windowMs = properties.getWindowSeconds() * 1000L;

        WindowSlot slot = slots.compute(key, (k, existing) -> {
            if (existing == null || existing.isExpired(nowMs, windowMs)) {
                return new WindowSlot(nowMs);
            }
            return existing;
        });

        return slot.tryConsume(limitPerWindow);
    }

    private static final class WindowSlot {
        private final long windowStartMs;
        private final AtomicInteger count = new AtomicInteger(0);

        WindowSlot(long windowStartMs) {
            this.windowStartMs = windowStartMs;
        }

        boolean isExpired(long nowMs, long windowMs) {
            return nowMs - windowStartMs >= windowMs;
        }

        boolean tryConsume(int limit) {
            int current = count.incrementAndGet();
            return current <= limit;
        }
    }
}
