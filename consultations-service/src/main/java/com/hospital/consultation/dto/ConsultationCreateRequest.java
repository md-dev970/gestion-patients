package com.hospital.consultation.dto;

import com.hospital.consultation.model.ConsultationType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO pour la création d'une consultation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationCreateRequest {

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    @NotNull(message = "User/Doctor ID is required")
    private Long userId;

    private LocalDateTime consultationDate;

    @NotNull(message = "Consultation type is required")
    private ConsultationType consultationType;

    private String motif;
    private String notes;
}
