package com.hospital.patient.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * No-op implementation when security.audit.url is not set (T6.3).
 */
@Component
@ConditionalOnMissingBean(com.hospital.patient.audit.HttpSecurityAuditSender.class)
public class NoOpSecurityAuditSender implements SecurityAuditSender {

    private static final Logger log = LoggerFactory.getLogger(NoOpSecurityAuditSender.class);

    @Override
    public void sendPhiDeleted(String resourceType, String resourceId) {
        if (log.isDebugEnabled()) {
            log.debug("PHI_DELETED (no-op audit): resourceType={}, resourceId={}", resourceType, resourceId);
        }
    }

    @Override
    public void sendDossierAccessed(String resourceId, String action) {
        if (log.isDebugEnabled()) {
            log.debug("DOSSIER_ACCESSED (no-op audit): resourceId={}, action={}", resourceId, action);
        }
    }

    @Override
    public void sendPhiAccessed(String resourceType, String resourceId, String action) {
        if (log.isDebugEnabled()) {
            log.debug("PHI_ACCESS (no-op audit): resourceType={}, resourceId={}, action={}", resourceType, resourceId, action);
        }
    }

    @Override
    public void sendRetentionPurgeCompleted(long purgedCount) {
        if (log.isDebugEnabled()) {
            log.debug("RETENTION_PURGE (no-op audit): purgedCount={}", purgedCount);
        }
    }
}
