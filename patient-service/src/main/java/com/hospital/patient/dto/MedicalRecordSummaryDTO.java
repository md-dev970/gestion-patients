package com.hospital.patient.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Lightweight projection of a patient's medical record for dossier export (T6.3).
 * Matches the JSON returned by medical-record-service /api/medical-records/patient/{patientId}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecordSummaryDTO {

    private Long id;
    private Long patientId;
    private String allergies;
    private String currentMedications;
    private String chronicConditions;
    private String familyHistory;

    /**
     * We keep entries as a raw list of maps/objects to avoid tight coupling
     * with medical-record-service DTOs. Jackson will bind the nested JSON.
     */
    private List<Object> entries;
}

