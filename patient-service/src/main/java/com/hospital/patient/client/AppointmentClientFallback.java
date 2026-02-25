package com.hospital.patient.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Fallback for {@link AppointmentClient}.
 * T6.2: Logs failure when appointment-service is unavailable during cascade erasure.
 */
@Component
@Slf4j
public class AppointmentClientFallback implements AppointmentClient {

    @Override
    public void deleteAppointmentsByPatientId(Long patientId) {
        log.warn("T6.2: Appointment Service unavailable. "
                + "Fallback triggered for deleteAppointmentsByPatientId({}).", patientId);
        // TODO: Implement retry/compensation strategy in resilience subject.
    }
}

