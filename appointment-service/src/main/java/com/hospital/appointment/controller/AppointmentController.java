package com.hospital.appointment.controller;

import com.hospital.appointment.dto.AppointmentCreateRequest;
import com.hospital.appointment.dto.AppointmentDTO;
import com.hospital.appointment.model.AppointmentStatus;
import com.hospital.appointment.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                       APPOINTMENT REST CONTROLLER                            ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS CLASS EXISTS:                                                      ║
 * ║  Exposes REST API endpoints for appointment operations.                      ║
 * ║                                                                              ║
 * ║  This endpoint is mandatory according to the Kit Commun                      ║
 * ║                                                                              ║
 * ║  Base URL: /api/appointments                                                 ║
 * ║                                                                              ║
 * ║  // Permissions will be checked in Subject 2                                 ║
 * ║  // Security will be reinforced in Subject 3                                 ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Slf4j
public class AppointmentController {

    private final AppointmentService appointmentService;

    /**
     * Creates a new appointment.
     * This endpoint is mandatory according to the Kit Commun.
     */
    @PostMapping
    public ResponseEntity<AppointmentDTO> createAppointment(
            @Valid @RequestBody AppointmentCreateRequest request) {
        log.info("REST request to create appointment");
        AppointmentDTO created = appointmentService.createAppointment(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * Retrieves an appointment by ID.
     * This endpoint is mandatory according to the Kit Commun.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentDTO> getAppointmentById(@PathVariable Long id) {
        log.info("REST request to get appointment: {}", id);
        return appointmentService.getAppointmentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieves appointments for a patient.
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByPatient(
            @PathVariable Long patientId) {
        log.info("REST request to get appointments for patient: {}", patientId);
        List<AppointmentDTO> appointments = appointmentService.getAppointmentsByPatient(patientId);
        return ResponseEntity.ok(appointments);
    }

    /**
     * Retrieves appointments for a doctor.
     */
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByDoctor(
            @PathVariable Long doctorId) {
        log.info("REST request to get appointments for doctor: {}", doctorId);
        List<AppointmentDTO> appointments = appointmentService.getAppointmentsByDoctor(doctorId);
        return ResponseEntity.ok(appointments);
    }

    /**
     * Retrieves appointments for a doctor on a specific date.
     * // Business logic will be added in the specialized subject
     */
    @GetMapping("/doctor/{doctorId}/date/{date}")
    public ResponseEntity<List<AppointmentDTO>> getDoctorAppointmentsForDate(
            @PathVariable Long doctorId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("REST request to get appointments for doctor {} on {}", doctorId, date);
        List<AppointmentDTO> appointments = appointmentService.getDoctorAppointmentsForDate(doctorId, date);
        return ResponseEntity.ok(appointments);
    }

    /**
     * Updates an appointment.
     * This endpoint is mandatory according to the Kit Commun.
     */
    @PutMapping("/{id}")
    public ResponseEntity<AppointmentDTO> updateAppointment(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentDTO appointmentDTO) {
        log.info("REST request to update appointment: {}", id);
        AppointmentDTO updated = appointmentService.updateAppointment(id, appointmentDTO);
        return ResponseEntity.ok(updated);
    }

    /**
     * Updates appointment status.
     * // Business logic will be added in the specialized subject
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<AppointmentDTO> updateAppointmentStatus(
            @PathVariable Long id,
            @RequestParam AppointmentStatus status) {
        log.info("REST request to update appointment {} status to {}", id, status);
        AppointmentDTO updated = appointmentService.updateAppointmentStatus(id, status);
        return ResponseEntity.ok(updated);
    }

    /**
     * Cancels an appointment.
     * // Permissions will be checked in Subject 2
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelAppointment(@PathVariable Long id) {
        log.info("REST request to cancel appointment: {}", id);
        appointmentService.cancelAppointment(id);
        return ResponseEntity.noContent().build();
    }
}

