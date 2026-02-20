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
 * Sends ACCESS_DENIED events to security-audit-log via HTTP POST.
 * Activated when security.audit.url is set.
 */
@Component
@Primary
@ConditionalOnProperty(name = "security.audit.url")
public class HttpSecurityAuditSender implements SecurityAuditSender {

    private static final Logger log = LoggerFactory.getLogger(HttpSecurityAuditSender.class);

    private final WebClient webClient;
    private final String auditUrl;

    public HttpSecurityAuditSender(WebClient.Builder webClientBuilder,
                                   @Value("${security.audit.url}") String auditUrl) {
        this.auditUrl = auditUrl;
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
        webClient.post()
                .uri(auditUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnError(e -> log.warn("Failed to send ACCESS_DENIED to audit log: {}", e.getMessage()))
                .onErrorResume(e -> Mono.empty())
                .subscribe();
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
        webClient.post()
                .uri(auditUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnError(e -> log.warn("Failed to send RATE_LIMIT_EXCEEDED to audit log: {}", e.getMessage()))
                .onErrorResume(e -> Mono.empty())
                .subscribe();
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
        webClient.post()
                .uri(auditUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnError(e -> log.warn("Failed to send SUSPICIOUS_INPUT to audit log: {}", e.getMessage()))
                .onErrorResume(e -> Mono.empty())
                .subscribe();
    }
}
