package com.hospital.patient.client;

import com.hospital.patient.dto.AppointmentSummaryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Feign client for Appointment Service.
 * T6.2: Used by Patient Service to cascade erase a patient's appointments.
 * T6.3: Used to fetch a patient's appointments for dossier export.
 */
@FeignClient(name = "appointment-service", fallback = AppointmentClientFallback.class)
public interface AppointmentClient {

    @DeleteMapping("/api/appointments/patient/{patientId}")
    void deleteAppointmentsByPatientId(@PathVariable("patientId") Long patientId);

    @GetMapping("/api/appointments/patient/{patientId}")
    List<AppointmentSummaryDTO> getAppointmentsByPatientId(@PathVariable("patientId") Long patientId);
}


