package com.hospital.patient.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Lightweight projection of a consultation for dossier export (T6.3).
 * Matches the main fields of consultations-service ConsultationDTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationSummaryDTO {

    private UUID consultationId;
    private Long patientId;
    private Long userId;
    private LocalDateTime consultationDate;
    private String consultationType;
    private String diagnostic;
    private String notes;
    private String motif;
    private String prescriptions;
    private String status;
}

