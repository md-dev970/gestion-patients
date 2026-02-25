package com.hospital.patient.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client for Consultations Service.
 * T6.2: Used by Patient Service to cascade erase a patient's consultations.
 */
@FeignClient(name = "consultations-service", fallback = ConsultationClientFallback.class)
public interface ConsultationClient {

    @DeleteMapping("/api/consultations/patient/{patientId}")
    void deleteConsultationsByPatientId(@PathVariable("patientId") Long patientId);
}

