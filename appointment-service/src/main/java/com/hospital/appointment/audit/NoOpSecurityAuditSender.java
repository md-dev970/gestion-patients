package com.hospital.appointment.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * No-op implementation when security.audit.url is not set (T6.3).
 */
@Component
@ConditionalOnMissingBean(com.hospital.appointment.audit.HttpSecurityAuditSender.class)
public class NoOpSecurityAuditSender implements SecurityAuditSender {

    private static final Logger log = LoggerFactory.getLogger(NoOpSecurityAuditSender.class);

    @Override
    public void sendPhiDeleted(String resourceType, String resourceId) {
        if (log.isDebugEnabled()) {
            log.debug("PHI_DELETED (no-op audit): resourceType={}, resourceId={}", resourceType, resourceId);
        }
    }
}
