package com.hospital.gateway.bruteforce;

import com.hospital.gateway.config.BruteforceByIpProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BruteforceByIpStore Unit Tests")
class BruteforceByIpStoreTest {

    private BruteforceByIpProperties properties;
    private BruteforceByIpStore store;

    @BeforeEach
    void setUp() {
        properties = new BruteforceByIpProperties();
        properties.setMaxFailedAttempts(3);
        properties.setLockoutDurationMinutes(15);
        store = new BruteforceByIpStore(properties);
    }

    @Test
    @DisplayName("isBlocked - initially false")
    void isBlocked_initiallyFalse() {
        assertThat(store.isBlocked("192.168.1.1")).isFalse();
    }

    @Test
    @DisplayName("recordFailure - under N - not blocked")
    void recordFailure_underN_notBlocked() {
        assertThat(store.recordFailure("10.0.0.1")).isFalse();
        assertThat(store.recordFailure("10.0.0.1")).isFalse();
        assertThat(store.isBlocked("10.0.0.1")).isFalse();
    }

    @Test
    @DisplayName("recordFailure - N failures - blocked and returns true on Nth")
    void recordFailure_nFailures_blockedAndReturnsTrueOnNth() {
        assertThat(store.recordFailure("10.0.0.2")).isFalse();
        assertThat(store.recordFailure("10.0.0.2")).isFalse();
        assertThat(store.recordFailure("10.0.0.2")).isTrue();
        assertThat(store.isBlocked("10.0.0.2")).isTrue();
    }

    @Test
    @DisplayName("different IPs independent")
    void differentIpsIndependent() {
        store.recordFailure("1.1.1.1");
        store.recordFailure("1.1.1.1");
        store.recordFailure("1.1.1.1");
        store.recordFailure("2.2.2.2");
        assertThat(store.isBlocked("1.1.1.1")).isTrue();
        assertThat(store.isBlocked("2.2.2.2")).isFalse();
    }

    @Test
    @DisplayName("null or blank IP - isBlocked false, recordFailure no effect")
    void nullOrBlankIp_safe() {
        assertThat(store.isBlocked(null)).isFalse();
        assertThat(store.isBlocked("")).isFalse();
        assertThat(store.recordFailure(null)).isFalse();
        assertThat(store.recordFailure("")).isFalse();
    }
}
