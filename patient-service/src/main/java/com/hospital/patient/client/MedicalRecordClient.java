package com.hospital.patient.client;

import com.hospital.patient.dto.MedicalRecordSummaryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client for Medical Record Service.
 * T6.2: Used by Patient Service to cascade erase a patient's medical records.
 * T6.3: Used to fetch the patient's medical record for dossier export.
 */
@FeignClient(name = "medical-record-service", fallback = MedicalRecordClientFallback.class)
public interface MedicalRecordClient {

    @DeleteMapping("/api/medical-records/patient/{patientId}")
    void deleteMedicalRecordsByPatientId(@PathVariable("patientId") Long patientId);

    @GetMapping("/api/medical-records/patient/{patientId}")
    MedicalRecordSummaryDTO getMedicalRecordByPatientId(@PathVariable("patientId") Long patientId);
}


