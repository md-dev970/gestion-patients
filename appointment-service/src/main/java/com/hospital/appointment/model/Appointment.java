package com.hospital.appointment.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                          APPOINTMENT ENTITY                                  ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS CLASS EXISTS:                                                      ║
 * ║  Represents a medical appointment in the hospital system.                    ║
 * ║  Links patients with medical staff at specific times.                        ║
 * ║                                                                              ║
 * ║  IMPORTANT: This entity stores IDs of patients/staff, NOT embedded objects.  ║
 * ║  WHY: Microservices pattern - each service owns its data.                    ║
 * ║  The actual patient/staff data is retrieved via Feign clients.               ║
 * ║                                                                              ║
 * ║  Students: Add additional fields based on your specialized subject.          ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
@Entity
@Table(name = "appointments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Reference to the patient (from Patient Service).
     * WHY Long instead of Patient: Microservices should not share domain models.
     * // Business logic will be added in the specialized subject
     */
    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    /**
     * Reference to the doctor/staff (from Staff Service).
     */
    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;

    /**
     * Appointment date and time.
     * This field is mandatory according to the Kit Commun.
     */
    @Column(name = "appointment_date_time", nullable = false)
    private LocalDateTime appointmentDateTime;

    /**
     * Duration in minutes.
     */
    @Column(name = "duration_minutes")
    private Integer durationMinutes = 30;

    /**
     * Current status of the appointment.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;

    /**
     * Type of appointment (consultation, follow-up, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "appointment_type")
    private AppointmentType appointmentType;

    /**
     * Reason for the appointment.
     */
    @Column(length = 500)
    private String reason;

    /**
     * Notes added by medical staff.
     * // Business logic will be added in the specialized subject
     */
    @Column(length = 1000)
    private String notes;

    /**
     * Room or location for the appointment.
     */
    @Column(name = "room_number")
    private String roomNumber;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

