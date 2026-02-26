package com.hospital.gateway.audit;

import com.hospital.gateway.rbac.Action;
import com.hospital.gateway.rbac.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * No-op implementation of SecurityAuditSender. Logs at DEBUG when ACCESS_DENIED would be sent.
 * Used when security.audit.url is not set; when set, HttpSecurityAuditSender is @Primary.
 */
@Component
@ConditionalOnMissingBean(name = "httpSecurityAuditSender")
public class NoOpSecurityAuditSender implements SecurityAuditSender {

    private static final Logger log = LoggerFactory.getLogger(NoOpSecurityAuditSender.class);

    @Override
    public void sendAccessDenied(String userId, Resource resourceType, String resourceId, Action action, String reason) {
        if (log.isDebugEnabled()) {
            log.debug("ACCESS_DENIED (no-op audit): userId={}, resourceType={}, resourceId={}, action={}, reason={}",
                    userId, resourceType, resourceId, action, reason);
        }
    }

    @Override
    public void sendRateLimitExceeded(String keyType, String key, long limit, long windowSeconds) {
        if (log.isDebugEnabled()) {
            log.debug("RATE_LIMIT_EXCEEDED (no-op audit): keyType={}, key={}, limit={}, windowSeconds={}",
                    keyType, key, limit, windowSeconds);
        }
    }

    @Override
    public void sendSuspiciousInput(String eventType, String source, String path, String method, String category) {
        if (log.isDebugEnabled()) {
            log.debug("SUSPICIOUS_INPUT (no-op audit): eventType={}, source={}, path={}, method={}, category={}",
                    eventType, source, path, method, category);
        }
    }

    @Override
    public void sendPatientSelfDeletionRequested(String patientId) {
        if (log.isDebugEnabled()) {
            log.debug("PATIENT_SELF_DELETION_REQUESTED (no-op audit): patientId={}", patientId);
        }
    }
}
