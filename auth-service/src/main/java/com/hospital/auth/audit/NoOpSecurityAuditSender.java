package com.hospital.auth.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * No-op implementation. Logs at DEBUG when ACCOUNT_LOCKED would be sent.
 */
@Component
@ConditionalOnMissingBean(name = "httpSecurityAuditSender")
public class NoOpSecurityAuditSender implements SecurityAuditSender {

    private static final Logger log = LoggerFactory.getLogger(NoOpSecurityAuditSender.class);

    @Override
    public void sendAccountLocked(long userId, String username, String reason) {
        if (log.isDebugEnabled()) {
            log.debug("ACCOUNT_LOCKED (no-op audit): userId={}, username={}, reason={}", userId, username, reason);
        }
    }
}
