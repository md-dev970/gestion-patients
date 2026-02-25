package com.hospital.patient.client;

import com.hospital.patient.dto.ConsultationSummaryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Feign client for Consultations Service.
 * T6.2: Used by Patient Service to cascade erase a patient's consultations.
 * T6.3: Used to fetch a patient's consultations for dossier export.
 */
@FeignClient(name = "consultations-service", fallback = ConsultationClientFallback.class)
public interface ConsultationClient {

    @DeleteMapping("/api/consultations/patient/{patientId}")
    void deleteConsultationsByPatientId(@PathVariable("patientId") Long patientId);

    @GetMapping("/api/consultations/patient/{patientId}")
    List<ConsultationSummaryDTO> getConsultationsByPatientId(@PathVariable("patientId") Long patientId);
}


