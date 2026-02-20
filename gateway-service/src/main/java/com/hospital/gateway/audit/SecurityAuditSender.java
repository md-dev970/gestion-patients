package com.hospital.gateway.audit;

import com.hospital.gateway.rbac.Action;
import com.hospital.gateway.rbac.Resource;

/**
 * Sends security audit events (e.g. ACCESS_DENIED) to the audit log.
 * Default implementation is no-op; an HTTP implementation can be used when
 * security-audit-log service is configured.
 */
public interface SecurityAuditSender {

    /**
     * Sends an ACCESS_DENIED event (pseudonymised, no PII/PHI).
     * Implementations should be non-blocking (fire-and-forget).
     */
    void sendAccessDenied(String userId, Resource resourceType, String resourceId, Action action, String reason);
}
