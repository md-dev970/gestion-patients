package com.hospital.consultation.dto;

import com.hospital.consultation.model.ConsultationStatus;
import com.hospital.consultation.model.ConsultationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO pour les consultations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationDTO {

    private UUID consultationId;
    private Long patientId;
    private Long userId;
    private LocalDateTime consultationDate;
    private ConsultationType consultationType;
    private String diagnostic;
    private String notes;
    private String motif;
    private String prescriptions;
    private ConsultationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
