package com.hospital.appointment.repository;

import com.hospital.appointment.model.Appointment;
import com.hospital.appointment.model.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                       APPOINTMENT REPOSITORY                                 ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS INTERFACE EXISTS:                                                  ║
 * ║  Data access layer for Appointment entities.                                 ║
 * ║                                                                              ║
 * ║  Students: Add custom query methods based on your specialized needs.         ║
 * ║  // Business logic will be added in the specialized subject                  ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /**
     * Finds all appointments for a patient.
     */
    List<Appointment> findByPatientIdOrderByAppointmentDateTimeDesc(Long patientId);

    /**
     * Finds all appointments for a doctor.
     */
    List<Appointment> findByDoctorIdOrderByAppointmentDateTimeAsc(Long doctorId);

    /**
     * Finds appointments by status.
     */
    List<Appointment> findByStatus(AppointmentStatus status);

    /**
     * Finds appointments for a doctor on a specific date range.
     * WHY: Used for checking doctor availability.
     * // Business logic will be added in the specialized subject
     */
    @Query("SELECT a FROM Appointment a WHERE a.doctorId = :doctorId " +
           "AND a.appointmentDateTime BETWEEN :start AND :end " +
           "AND a.status NOT IN ('CANCELLED', 'NO_SHOW')")
    List<Appointment> findDoctorAppointmentsInRange(
            @Param("doctorId") Long doctorId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * Finds appointments for a patient in a date range.
     */
    List<Appointment> findByPatientIdAndAppointmentDateTimeBetween(
            Long patientId, LocalDateTime start, LocalDateTime end);

    /**
     * Checks for conflicting appointments.
     * // Business logic will be added in the specialized subject
     */
    @Query("SELECT a FROM Appointment a WHERE a.doctorId = :doctorId " +
           "AND a.appointmentDateTime = :dateTime " +
           "AND a.status = 'SCHEDULED'")
    List<Appointment> findConflictingAppointments(
            @Param("doctorId") Long doctorId,
            @Param("dateTime") LocalDateTime dateTime);

    /**
     * Finds upcoming appointments for a patient.
     */
    @Query("SELECT a FROM Appointment a WHERE a.patientId = :patientId " +
           "AND a.appointmentDateTime > :now " +
           "AND a.status = 'SCHEDULED' " +
           "ORDER BY a.appointmentDateTime ASC")
    List<Appointment> findUpcomingAppointments(
            @Param("patientId") Long patientId,
            @Param("now") LocalDateTime now);
}

