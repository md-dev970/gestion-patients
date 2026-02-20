package com.hospital.auth.audit;

/**
 * Sends security audit events (e.g. ACCOUNT_LOCKED/BRUTEFORCE) to IDS/audit log.
 * Default implementation is no-op; HTTP implementation when security.audit.url is set.
 */
public interface SecurityAuditSender {

    /**
     * Sends an ACCOUNT_LOCKED event (e.g. after bruteforce detection).
     * Implementations should be non-blocking (fire-and-forget).
     */
    void sendAccountLocked(long userId, String username, String reason);
}
