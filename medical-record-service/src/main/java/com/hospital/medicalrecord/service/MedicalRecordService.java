package com.hospital.medicalrecord.service;

import com.hospital.medicalrecord.dto.MedicalEntryDTO;
import com.hospital.medicalrecord.dto.MedicalRecordDTO;

import java.util.Optional;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                     MEDICAL RECORD SERVICE INTERFACE                         ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS INTERFACE EXISTS:                                                  ║
 * ║  Defines the contract for medical record operations.                         ║
 * ║                                                                              ║
 * ║  // Security will be reinforced in Subject 3                                 ║
 * ║  // Permissions will be checked in Subject 2                                 ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public interface MedicalRecordService {

    /**
     * Creates a new medical record for a patient.
     * This endpoint is mandatory according to the Kit Commun.
     */
    MedicalRecordDTO createMedicalRecord(Long patientId);

    /**
     * Retrieves a medical record by ID.
     * This endpoint is mandatory according to the Kit Commun.
     */
    Optional<MedicalRecordDTO> getMedicalRecordById(Long id);

    /**
     * Retrieves a medical record by patient ID.
     * // Permissions will be checked in Subject 2
     */
    Optional<MedicalRecordDTO> getMedicalRecordByPatientId(Long patientId);

    /**
     * Updates medical record general information.
     * This endpoint is mandatory according to the Kit Commun.
     */
    MedicalRecordDTO updateMedicalRecord(Long id, MedicalRecordDTO recordDTO);

    /**
     * Adds a new medical entry to a record.
     * // Business logic will be added in the specialized subject
     */
    MedicalEntryDTO addEntry(Long patientId, MedicalEntryDTO entryDTO);

    /**
     * Retrieves a specific entry.
     */
    Optional<MedicalEntryDTO> getEntryById(Long entryId);

    /**
     * Gets or creates a medical record for a patient.
     */
    MedicalRecordDTO getOrCreateMedicalRecord(Long patientId);
}

