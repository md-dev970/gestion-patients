package com.hospital.gateway.audit;

import com.hospital.gateway.rbac.Action;
import com.hospital.gateway.rbac.Resource;

/**
 * Sends security audit events (e.g. ACCESS_DENIED, RATE_LIMIT_EXCEEDED) to the audit log or IDS.
 * Default implementation is no-op; an HTTP implementation can be used when
 * security.audit.url is configured.
 */
public interface SecurityAuditSender {

    /**
     * Sends an ACCESS_DENIED event (pseudonymised, no PII/PHI).
     * Implementations should be non-blocking (fire-and-forget).
     */
    void sendAccessDenied(String userId, Resource resourceType, String resourceId, Action action, String reason);

    /**
     * Sends a RATE_LIMIT_EXCEEDED event (for IDS). Key type is "IP" or "USER"; key is the identifier.
     * Implementations should be non-blocking (fire-and-forget).
     */
    void sendRateLimitExceeded(String keyType, String key, long limit, long windowSeconds);

    /**
     * Sends a SUSPICIOUS_INPUT event (for IDS) when SQLi/XSS-like patterns are detected.
     * No PII or raw payload in the event.
     * Implementations should be non-blocking (fire-and-forget).
     *
     * @param eventType e.g. "SUSPICIOUS_INPUT"
     * @param source    "query", "header", or "body"
     * @param path      request path
     * @param method    HTTP method
     * @param category  "SQLI" or "XSS"
     */
    void sendSuspiciousInput(String eventType, String source, String path, String method, String category);

    /**
     * Sends a PATIENT_SELF_DELETION_REQUESTED event when a ROLE_PATIENT is allowed to DELETE their own record (T6.11).
     * Enables the audit log to distinguish patient-initiated from admin-initiated deletion.
     * Implementations should be non-blocking (fire-and-forget). No PII in the payload.
     *
     * @param patientId the patient ID (resource id from path)
     */
    void sendPatientSelfDeletionRequested(String patientId);
}
