package com.hospital.appointment.dto;

import com.hospital.appointment.model.AppointmentType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                   APPOINTMENT CREATE REQUEST DTO                             ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS CLASS EXISTS:                                                      ║
 * ║  Separate DTO for creating appointments.                                     ║
 * ║  Contains only the fields needed for creation.                               ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentCreateRequest {

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    @NotNull(message = "Appointment date/time is required")
    @Future(message = "Appointment must be in the future")
    private LocalDateTime appointmentDateTime;

    @Min(value = 15, message = "Minimum duration is 15 minutes")
    private Integer durationMinutes = 30;

    private AppointmentType appointmentType;

    @Size(max = 500)
    private String reason;

    private String roomNumber;
}

