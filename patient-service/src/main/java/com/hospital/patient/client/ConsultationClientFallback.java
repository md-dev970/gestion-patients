package com.hospital.patient.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Fallback for {@link ConsultationClient}.
 * T6.2: Logs failure when consultations-service is unavailable during cascade erasure.
 */
@Component
@Slf4j
public class ConsultationClientFallback implements ConsultationClient {

    @Override
    public void deleteConsultationsByPatientId(Long patientId) {
        log.warn("T6.2: Consultations Service unavailable. "
                + "Fallback triggered for deleteConsultationsByPatientId({}).", patientId);
        // TODO: Implement retry/compensation strategy in resilience subject.
    }

    @Override
    public java.util.List<com.hospital.patient.dto.ConsultationSummaryDTO> getConsultationsByPatientId(Long patientId) {
        log.warn("T6.3: Consultations Service unavailable. "
                + "Fallback triggered for getConsultationsByPatientId({}).", patientId);
        return java.util.Collections.emptyList();
    }
}

