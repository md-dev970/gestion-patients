package com.hospital.patient.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client for Appointment Service.
 * T6.2: Used by Patient Service to cascade erase a patient's appointments.
 */
@FeignClient(name = "appointment-service", fallback = AppointmentClientFallback.class)
public interface AppointmentClient {

    @DeleteMapping("/api/appointments/patient/{patientId}")
    void deleteAppointmentsByPatientId(@PathVariable("patientId") Long patientId);
}

