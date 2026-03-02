package com.hospital.auth.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Sends ACCOUNT_LOCKED events to security-audit-log via HTTP POST.
 * Activated when security.audit.url is set. Runs POST fire-and-forget.
 */
@Component
@Primary
@ConditionalOnProperty(name = "security.audit.url")
public class HttpSecurityAuditSender implements SecurityAuditSender {

    private static final Logger log = LoggerFactory.getLogger(HttpSecurityAuditSender.class);

    private final RestTemplate restTemplate;
    private final String auditUrl;

    public HttpSecurityAuditSender(RestTemplate restTemplate,
                                   @Value("${security.audit.url}") String auditUrl) {
        this.restTemplate = restTemplate;
        this.auditUrl = auditUrl;
    }

    @Override
    public void sendAccountLocked(long userId, String username, String reason) {
        // username omitted from payload (no PII in audit events)
        Map<String, Object> payload = Map.of(
                "eventType", "ACCOUNT_LOCKED",
                "timestamp", Instant.now().toString(),
                "userId", String.valueOf(userId),
                "reason", reason != null ? reason : "BRUTEFORCE"
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        CompletableFuture.runAsync(() -> {
            try {
                restTemplate.postForObject(auditUrl, request, Void.class);
            } catch (Exception e) {
                log.warn("Failed to send ACCOUNT_LOCKED to audit log: {}", e.getMessage());
            }
        });
    }
}
