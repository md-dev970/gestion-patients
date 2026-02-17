package com.hospital.patient.model;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                            GENDER ENUMERATION                                ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS ENUM EXISTS:                                                       ║
 * ║  Provides type-safe gender values for patient records.                       ║
 * ║  Using an enum prevents invalid data entry.                                  ║
 * ║                                                                              ║
 * ║  Students: Extend this enum based on your specialized requirements.          ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public enum Gender {
    MALE,
    FEMALE,
    OTHER,
    PREFER_NOT_TO_SAY
}

