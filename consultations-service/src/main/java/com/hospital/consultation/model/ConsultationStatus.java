package com.hospital.consultation.model;

/**
 * Statuts possibles d'une consultation.
 */
public enum ConsultationStatus {
    /**
     * Consultation planifiée.
     */
    SCHEDULED,
    
    /**
     * Consultation en cours.
     */
    IN_PROGRESS,
    
    /**
     * Consultation terminée.
     */
    COMPLETED,
    
    /**
     * Consultation annulée.
     */
    CANCELLED,
    
    /**
     * Patient absent (no-show).
     */
    NO_SHOW
}
