package com.hospital.medicalrecord.model;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                        ENTRY TYPE ENUMERATION                                ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS ENUM EXISTS:                                                       ║
 * ║  Categorizes different types of medical entries.                             ║
 * ║  Each type may require different fields and validations.                     ║
 * ║                                                                              ║
 * ║  Students: Add more types based on your specialized requirements.            ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public enum EntryType {
    /**
     * Regular consultation/visit.
     */
    CONSULTATION,
    
    /**
     * Diagnosis made.
     */
    DIAGNOSIS,
    
    /**
     * Medical procedure performed.
     */
    PROCEDURE,
    
    /**
     * Prescription issued.
     */
    PRESCRIPTION,
    
    /**
     * Lab test result.
     */
    LAB_RESULT,
    
    /**
     * Imaging study result (X-ray, MRI, etc.)
     */
    IMAGING,
    
    /**
     * Vaccination record.
     */
    VACCINATION,
    
    /**
     * Hospital admission.
     */
    ADMISSION,
    
    /**
     * Hospital discharge.
     */
    DISCHARGE,
    
    /**
     * Follow-up notes.
     */
    FOLLOW_UP
}

