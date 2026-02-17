package com.hospital.appointment.dto;

import com.hospital.appointment.model.AppointmentStatus;
import com.hospital.appointment.model.AppointmentType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                      APPOINTMENT DATA TRANSFER OBJECT                        ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS CLASS EXISTS:                                                      ║
 * ║  DTO for transferring appointment data between layers.                       ║
 * ║                                                                              ║
 * ║  Students: Note that we use IDs for patient/doctor, not embedded objects.    ║
 * ║  The frontend can fetch full details from respective services if needed.     ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentDTO {

    private Long id;

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    @NotNull(message = "Appointment date/time is required")
    @Future(message = "Appointment must be in the future")
    private LocalDateTime appointmentDateTime;

    @Min(value = 15, message = "Minimum duration is 15 minutes")
    @Max(value = 120, message = "Maximum duration is 120 minutes")
    private Integer durationMinutes;

    private AppointmentStatus status;

    private AppointmentType appointmentType;

    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    private String reason;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;

    private String roomNumber;
    
    // ═══════════════════════════════════════════════════════════════════════════
    // These fields are populated by calling other services (Feign clients)
    // // Business logic will be added in the specialized subject
    // ═══════════════════════════════════════════════════════════════════════════
    private String patientName;   // Fetched from Patient Service
    private String doctorName;    // Fetched from Staff Service
}

