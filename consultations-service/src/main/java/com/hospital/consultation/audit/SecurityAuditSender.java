package com.hospital.consultation.audit;

/**
 * Sends security audit events for PHI operations.
 * No-op when security.audit.url is not set; HTTP implementation when configured (T6.3).
 */
public interface SecurityAuditSender {

    /**
     * Sends a PHI_DELETED event after successful deletion of PHI.
     * Implementations should be non-blocking (fire-and-forget).
     *
     * @param resourceType e.g. CONSULTATION
     * @param resourceId   identifier of the deleted resource (no PII)
     */
    void sendPhiDeleted(String resourceType, String resourceId);

    /**
     * Sends a PHI_ACCESS event after successful READ, CREATE or UPDATE of PHI (T1.14).
     *
     * @param resourceType e.g. CONSULTATION
     * @param resourceId   identifier (no PII)
     * @param action       READ, CREATE or UPDATE
     */
    void sendPhiAccessed(String resourceType, String resourceId, String action);
}
