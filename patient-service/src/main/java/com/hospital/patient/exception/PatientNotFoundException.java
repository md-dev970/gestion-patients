package com.hospital.patient.exception;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                    PATIENT NOT FOUND EXCEPTION                               ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS CLASS EXISTS:                                                      ║
 * ║  Custom exception for when a patient is not found.                           ║
 * ║                                                                              ║
 * ║  WHY custom exceptions?                                                      ║
 * ║    1. Clearer error handling                                                 ║
 * ║    2. Can be caught and translated to HTTP 404                               ║
 * ║    3. Better debugging information                                           ║
 * ║                                                                              ║
 * ║  Students: Create similar exceptions for other error cases.                  ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class PatientNotFoundException extends RuntimeException {

    public PatientNotFoundException(String message) {
        super(message);
    }

    public PatientNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

