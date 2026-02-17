package com.hospital.appointment.service;

import com.hospital.appointment.dto.AppointmentCreateRequest;
import com.hospital.appointment.dto.AppointmentDTO;
import com.hospital.appointment.model.AppointmentStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                      APPOINTMENT SERVICE INTERFACE                           ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS INTERFACE EXISTS:                                                  ║
 * ║  Defines the contract for appointment-related business operations.           ║
 * ║                                                                              ║
 * ║  // Business logic will be added in the specialized subject                  ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public interface AppointmentService {

    /**
     * Creates a new appointment.
     * This endpoint is mandatory according to the Kit Commun.
     */
    AppointmentDTO createAppointment(AppointmentCreateRequest request);

    /**
     * Retrieves an appointment by ID.
     * This endpoint is mandatory according to the Kit Commun.
     */
    Optional<AppointmentDTO> getAppointmentById(Long id);

    /**
     * Retrieves all appointments for a patient.
     */
    List<AppointmentDTO> getAppointmentsByPatient(Long patientId);

    /**
     * Retrieves all appointments for a doctor.
     */
    List<AppointmentDTO> getAppointmentsByDoctor(Long doctorId);

    /**
     * Retrieves appointments for a doctor on a specific date.
     * // Business logic will be added in the specialized subject
     */
    List<AppointmentDTO> getDoctorAppointmentsForDate(Long doctorId, LocalDate date);

    /**
     * Updates an appointment.
     * This endpoint is mandatory according to the Kit Commun.
     */
    AppointmentDTO updateAppointment(Long id, AppointmentDTO appointmentDTO);

    /**
     * Updates appointment status.
     */
    AppointmentDTO updateAppointmentStatus(Long id, AppointmentStatus status);

    /**
     * Cancels an appointment.
     */
    void cancelAppointment(Long id);

    /**
     * Checks if a time slot is available for a doctor.
     * // Business logic will be added in the specialized subject
     */
    boolean isTimeSlotAvailable(Long doctorId, java.time.LocalDateTime dateTime);
}

