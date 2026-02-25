package com.hospital.patient.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Lightweight projection of an appointment for dossier export (T6.3).
 * Matches the main fields of appointment-service AppointmentDTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentSummaryDTO {

    private Long id;
    private Long patientId;
    private Long doctorId;
    private LocalDateTime appointmentDateTime;
    private Integer durationMinutes;
    private String status;
    private String appointmentType;
    private String reason;
    private String notes;
    private String roomNumber;
}

