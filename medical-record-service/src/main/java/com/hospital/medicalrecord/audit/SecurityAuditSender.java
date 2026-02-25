package com.hospital.medicalrecord.audit;

/**
 * Sends security audit events for PHI operations.
 * No-op when security.audit.url is not set; HTTP implementation when configured (T6.3).
 */
public interface SecurityAuditSender {

    /**
     * Sends a PHI_DELETED event after successful deletion of PHI.
     * Implementations should be non-blocking (fire-and-forget).
     *
     * @param resourceType e.g. MEDICAL_RECORD
     * @param resourceId   identifier of the deleted resource (no PII)
     */
    void sendPhiDeleted(String resourceType, String resourceId);
}
