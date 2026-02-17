package com.hospital.appointment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                          PATIENT FEIGN CLIENT                                ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS INTERFACE EXISTS:                                                  ║
 * ║  Enables this service to call the Patient Service.                           ║
 * ║                                                                              ║
 * ║  WHY Feign?                                                                  ║
 * ║    1. Declarative REST client (just define interface, Spring implements)    ║
 * ║    2. Integrates with Eureka for service discovery                           ║
 * ║    3. Built-in load balancing                                                ║
 * ║                                                                              ║
 * ║  Students: Use Feign clients to communicate between microservices.           ║
 * ║  // Business logic will be added in the specialized subject                  ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
@FeignClient(name = "patient-service", fallback = PatientClientFallback.class)
public interface PatientClient {

    /**
     * Checks if a patient exists.
     * WHY: Validate patient ID before creating appointment.
     */
    @GetMapping("/api/patients/{id}/exists")
    Boolean checkPatientExists(@PathVariable("id") Long patientId);

    /**
     * Gets patient details.
     * // Business logic will be added in the specialized subject
     * TODO: Define a PatientResponse DTO to receive patient data
     */
    // @GetMapping("/api/patients/{id}")
    // PatientResponse getPatient(@PathVariable("id") Long patientId);
}

