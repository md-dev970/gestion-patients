package com.hospital.patient.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client for Medical Record Service.
 * T6.2: Used by Patient Service to cascade erase a patient's medical records.
 */
@FeignClient(name = "medical-record-service", fallback = MedicalRecordClientFallback.class)
public interface MedicalRecordClient {

    @DeleteMapping("/api/medical-records/patient/{patientId}")
    void deleteMedicalRecordsByPatientId(@PathVariable("patientId") Long patientId);
}

