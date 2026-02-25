package com.hospital.patient.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Aggregated view of all data related to a patient (T6.3).
 * Combines core patient info with medical record, consultations and appointments.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientDossierDTO {

    private PatientDTO patient;
    private MedicalRecordSummaryDTO medicalRecord;
    private List<ConsultationSummaryDTO> consultations;
    private List<AppointmentSummaryDTO> appointments;
}

