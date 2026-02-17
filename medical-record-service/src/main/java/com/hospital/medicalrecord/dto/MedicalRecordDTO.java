package com.hospital.medicalrecord.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                     MEDICAL RECORD DATA TRANSFER OBJECT                      ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS CLASS EXISTS:                                                      ║
 * ║  DTO for transferring medical record data.                                   ║
 * ║  Contains the patient's complete medical dossier.                            ║
 * ║                                                                              ║
 * ║  // Security will be reinforced in Subject 3                                 ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecordDTO {

    private Long id;

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    private String allergies;

    private String currentMedications;

    private String chronicConditions;

    private String familyHistory;

    private List<MedicalEntryDTO> entries;

    // Populated from Patient Service
    private String patientName;
}

