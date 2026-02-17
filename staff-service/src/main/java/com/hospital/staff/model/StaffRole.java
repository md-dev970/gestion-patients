package com.hospital.staff.model;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                          STAFF ROLE ENUMERATION                              ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS ENUM EXISTS:                                                       ║
 * ║  Defines the different roles/positions of hospital staff.                    ║
 * ║                                                                              ║
 * ║  // Permissions will be checked in Subject 2                                 ║
 * ║  Students: Each role will have different permissions in the system.          ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public enum StaffRole {
    /**
     * Medical doctor - can diagnose and treat patients.
     */
    DOCTOR,
    
    /**
     * Registered nurse - provides patient care.
     */
    NURSE,
    
    /**
     * System administrator - manages the hospital system.
     */
    ADMIN,
    
    /**
     * Reception staff - handles patient registration.
     */
    RECEPTIONIST,
    
    /**
     * Laboratory technician - performs lab tests.
     */
    LAB_TECHNICIAN,
    
    /**
     * Pharmacist - manages medication.
     */
    PHARMACIST,
    
    /**
     * Radiologist - performs imaging studies.
     */
    RADIOLOGIST
}

