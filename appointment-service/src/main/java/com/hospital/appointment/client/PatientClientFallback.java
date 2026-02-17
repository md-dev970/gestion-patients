package com.hospital.appointment.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                      PATIENT CLIENT FALLBACK                                 ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS CLASS EXISTS:                                                      ║
 * ║  Provides fallback behavior when Patient Service is unavailable.             ║
 * ║                                                                              ║
 * ║  WHY fallbacks?                                                              ║
 * ║    - Circuit Breaker pattern: prevents cascading failures                    ║
 * ║    - Graceful degradation: system continues working partially                ║
 * ║                                                                              ║
 * ║  Students: Implement proper fallback logic in Subject 4 (Resilience).        ║
 * ║  // Business logic will be added in the specialized subject                  ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
@Component
@Slf4j
public class PatientClientFallback implements PatientClient {

    @Override
    public Boolean checkPatientExists(Long patientId) {
        log.warn("Patient Service unavailable. Fallback triggered for patient ID: {}", patientId);
        // TODO: Implement proper fallback strategy
        // Option 1: Return cached result
        // Option 2: Return true and validate later
        // Option 3: Throw a specific exception
        return true; // Temporary: assume patient exists
    }
}

