package com.hospital.gateway.ratelimit;

import com.hospital.gateway.config.RateLimitProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RateLimitStore Unit Tests")
class RateLimitStoreTest {

    private RateLimitProperties properties;
    private RateLimitStore store;

    @BeforeEach
    void setUp() {
        properties = new RateLimitProperties();
        properties.setRequestsPerMinutePerIp(3);
        properties.setRequestsPerMinutePerUser(2);
        properties.setWindowSeconds(60);
        store = new RateLimitStore(properties);
    }

    @Test
    @DisplayName("tryConsume under limit - all allowed")
    void tryConsume_underLimit_allAllowed() {
        String key = "ip:192.168.1.1";
        assertThat(store.tryConsume(key, 3)).isTrue();
        assertThat(store.tryConsume(key, 3)).isTrue();
        assertThat(store.tryConsume(key, 3)).isTrue();
    }

    @Test
    @DisplayName("tryConsume over limit - fourth denied")
    void tryConsume_overLimit_fourthDenied() {
        String key = "ip:10.0.0.1";
        assertThat(store.tryConsume(key, 3)).isTrue();
        assertThat(store.tryConsume(key, 3)).isTrue();
        assertThat(store.tryConsume(key, 3)).isTrue();
        assertThat(store.tryConsume(key, 3)).isFalse();
        assertThat(store.tryConsume(key, 3)).isFalse();
    }

    @Test
    @DisplayName("different keys are independent")
    void tryConsume_differentKeys_independent() {
        assertThat(store.tryConsume("ip:1.2.3.4", 1)).isTrue();
        assertThat(store.tryConsume("ip:1.2.3.4", 1)).isFalse();
        assertThat(store.tryConsume("user:42", 1)).isTrue();
        assertThat(store.tryConsume("user:42", 1)).isFalse();
        assertThat(store.tryConsume("ip:5.6.7.8", 1)).isTrue();
    }

    @Test
    @DisplayName("after window expires - requests allowed again")
    void tryConsume_afterWindowExpires_allowedAgain() throws InterruptedException {
        properties.setWindowSeconds(1);
        store = new RateLimitStore(properties);
        String key = "ip:127.0.0.1";
        assertThat(store.tryConsume(key, 1)).isTrue();
        assertThat(store.tryConsume(key, 1)).isFalse();
        Thread.sleep(1100);
        assertThat(store.tryConsume(key, 1)).isTrue();
        assertThat(store.tryConsume(key, 1)).isFalse();
    }
}
