package com.hospital.patient.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;

/**
 * Sends PHI_DELETED events to the audit URL when security.audit.url is set (T6.3).
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
    public void sendPhiDeleted(String resourceType, String resourceId) {
        Map<String, Object> payload = Map.of(
                "eventType", "PHI_DELETED",
                "timestamp", Instant.now().toString(),
                "resourceType", resourceType != null ? resourceType : "",
                "resourceId", resourceId != null ? resourceId : ""
        );
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            restTemplate.postForObject(auditUrl, new HttpEntity<>(payload, headers), Void.class);
        } catch (Exception e) {
            log.warn("Failed to send PHI_DELETED to audit log: {}", e.getMessage());
        }
    }

    @Override
    public void sendDossierAccessed(String resourceId, String action) {
        Map<String, Object> payload = Map.of(
                "eventType", "DOSSIER_ACCESSED",
                "timestamp", Instant.now().toString(),
                "resourceType", "PATIENT_DOSSIER",
                "resourceId", resourceId != null ? resourceId : "",
                "action", action != null ? action : "READ"
        );
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            restTemplate.postForObject(auditUrl, new HttpEntity<>(payload, headers), Void.class);
        } catch (Exception e) {
            log.warn("Failed to send DOSSIER_ACCESSED to audit log: {}", e.getMessage());
        }
    }
}
