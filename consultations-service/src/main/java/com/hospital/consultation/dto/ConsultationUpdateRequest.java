package com.hospital.consultation.dto;

import com.hospital.consultation.model.ConsultationStatus;
import com.hospital.consultation.model.ConsultationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO pour la mise à jour d'une consultation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationUpdateRequest {

    private LocalDateTime consultationDate;
    private ConsultationType consultationType;
    private String diagnostic;
    private String notes;
    private String motif;
    private String prescriptions;
    private ConsultationStatus status;
}
