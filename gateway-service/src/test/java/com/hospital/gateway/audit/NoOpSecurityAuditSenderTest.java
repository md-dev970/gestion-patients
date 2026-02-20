package com.hospital.gateway.audit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("NoOpSecurityAuditSender Unit Tests")
class NoOpSecurityAuditSenderTest {

    private NoOpSecurityAuditSender sender;

    @BeforeEach
    void setUp() {
        sender = new NoOpSecurityAuditSender();
    }

    @Test
    @DisplayName("sendSuspiciousInput does not throw")
    void sendSuspiciousInput_doesNotThrow() {
        assertThatCode(() -> sender.sendSuspiciousInput(
                "SUSPICIOUS_INPUT", "query", "/api/patients/search", "GET", "SQLI"))
                .doesNotThrowAnyException();
    }
}
