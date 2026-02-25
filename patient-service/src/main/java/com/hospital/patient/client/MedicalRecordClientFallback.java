package com.hospital.patient.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Fallback for {@link MedicalRecordClient}.
 * T6.2: Logs failure when medical-record-service is unavailable during cascade erasure.
 */
@Component
@Slf4j
public class MedicalRecordClientFallback implements MedicalRecordClient {

    @Override
    public void deleteMedicalRecordsByPatientId(Long patientId) {
        log.warn("T6.2: Medical Record Service unavailable. "
                + "Fallback triggered for deleteMedicalRecordsByPatientId({}).", patientId);
        // TODO: Implement retry/compensation strategy in resilience subject.
    }
}

