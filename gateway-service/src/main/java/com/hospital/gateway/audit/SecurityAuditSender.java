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
}
