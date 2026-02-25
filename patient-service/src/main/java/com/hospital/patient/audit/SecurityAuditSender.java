package com.hospital.patient.audit;

/**
 * Sends security audit events for PHI operations.
 * No-op when security.audit.url is not set; HTTP implementation when configured (T6.3, T6.8).
 */
public interface SecurityAuditSender {

    /**
     * Sends a PHI_DELETED event after successful deletion of PHI.
     * Implementations should be non-blocking (fire-and-forget).
     *
     * @param resourceType e.g. PATIENT
     * @param resourceId   identifier of the deleted resource (no PII)
     */
    void sendPhiDeleted(String resourceType, String resourceId);

    /**
     * Sends a DOSSIER_ACCESSED event when a patient dossier is read or exported (T6.8).
     * Implementations should be non-blocking (fire-and-forget). No PII in the event.
     *
     * @param resourceId patient ID (no PII)
     * @param action     "READ" for GET dossier, "EXPORT" for GET dossier/export
     */
    void sendDossierAccessed(String resourceId, String action);
}
