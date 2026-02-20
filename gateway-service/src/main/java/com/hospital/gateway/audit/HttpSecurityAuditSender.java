package com.hospital.gateway.audit;

import com.hospital.gateway.rbac.Action;
import com.hospital.gateway.rbac.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

/**
 * Sends events to security-audit-log and optionally to ids-service (T1.9).
 * Activated when security.audit.url is set. When security.ids.url is set,
 * RATE_LIMIT_EXCEEDED and SUSPICIOUS_INPUT are also POSTed to the IDS URL.
 */
@Component
@Primary
@ConditionalOnProperty(name = "security.audit.url")
public class HttpSecurityAuditSender implements SecurityAuditSender {

    private static final Logger log = LoggerFactory.getLogger(HttpSecurityAuditSender.class);

    private final WebClient webClient;
    private final String auditUrl;
    private final String idsUrl;

    public HttpSecurityAuditSender(WebClient.Builder webClientBuilder,
                                   @Value("${security.audit.url}") String auditUrl,
                                   @Value("${security.ids.url:}") String idsUrl) {
        this.auditUrl = auditUrl;
        this.idsUrl = idsUrl != null && !idsUrl.isBlank() ? idsUrl.trim() : null;
        this.webClient = webClientBuilder.build();
    }

    @Override
    public void sendAccessDenied(String userId, Resource resourceType, String resourceId, Action action, String reason) {
        Map<String, Object> payload = Map.of(
                "eventType", "ACCESS_DENIED",
                "timestamp", Instant.now().toString(),
                "userId", userId != null ? userId : "",
                "resourceType", resourceType.name(),
                "resourceId", resourceId != null ? resourceId : "",
                "action", action.name(),
                "reason", reason != null ? reason : "RBAC_DENY"
        );
        postToAudit(payload, "ACCESS_DENIED");
    }

    @Override
    public void sendRateLimitExceeded(String keyType, String key, long limit, long windowSeconds) {
        Map<String, Object> payload = Map.of(
                "eventType", "RATE_LIMIT_EXCEEDED",
                "timestamp", Instant.now().toString(),
                "keyType", keyType != null ? keyType : "",
                "key", key != null ? key : "",
                "limit", limit,
                "windowSeconds", windowSeconds
        );
        postToAudit(payload, "RATE_LIMIT_EXCEEDED");
        if (idsUrl != null) {
            postToIds(payload, "RATE_LIMIT_EXCEEDED");
        }
    }

    @Override
    public void sendSuspiciousInput(String eventType, String source, String path, String method, String category) {
        Map<String, Object> payload = Map.of(
                "eventType", eventType != null ? eventType : "SUSPICIOUS_INPUT",
                "timestamp", Instant.now().toString(),
                "source", source != null ? source : "",
                "path", path != null ? path : "",
                "method", method != null ? method : "",
                "category", category != null ? category : ""
        );
        postToAudit(payload, "SUSPICIOUS_INPUT");
        if (idsUrl != null) {
            postToIds(payload, "SUSPICIOUS_INPUT");
        }
    }

    private void postToAudit(Map<String, Object> payload, String eventLabel) {
        webClient.post()
                .uri(auditUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnError(e -> log.warn("Failed to send {} to audit log: {}", eventLabel, e.getMessage()))
                .onErrorResume(e -> Mono.empty())
                .subscribe();
    }

    private void postToIds(Map<String, Object> payload, String eventLabel) {
        webClient.post()
                .uri(idsUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnError(e -> log.warn("Failed to send {} to IDS: {}", eventLabel, e.getMessage()))
                .onErrorResume(e -> Mono.empty())
                .subscribe();
    }
}
