package com.hospital.patient.exception;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                    DUPLICATE PATIENT EXCEPTION                               ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS CLASS EXISTS:                                                      ║
 * ║  Thrown when attempting to create a patient that already exists.             ║
 * ║  (e.g., duplicate national ID)                                               ║
 * ║                                                                              ║
 * ║  Students: This should result in HTTP 409 (Conflict).                        ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class DuplicatePatientException extends RuntimeException {

    public DuplicatePatientException(String message) {
        super(message);
    }

    public DuplicatePatientException(String message, Throwable cause) {
        super(message, cause);
    }
}

