package com.hospital.medicalrecord.controller;

import com.hospital.medicalrecord.dto.MedicalEntryDTO;
import com.hospital.medicalrecord.dto.MedicalRecordDTO;
import com.hospital.medicalrecord.service.MedicalRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                     MEDICAL RECORD REST CONTROLLER                           ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS CLASS EXISTS:                                                      ║
 * ║  Exposes REST API endpoints for medical record operations.                   ║
 * ║                                                                              ║
 * ║  This endpoint is mandatory according to the Kit Commun                      ║
 * ║                                                                              ║
 * ║  Base URL: /api/medical-records                                              ║
 * ║                                                                              ║
 * ║  IMPORTANT: This controller handles sensitive medical data.                  ║
 * ║  // Security will be reinforced in Subject 3                                 ║
 * ║  // Permissions will be checked in Subject 2                                 ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
@RestController
@RequestMapping("/api/medical-records")
@RequiredArgsConstructor
@Slf4j
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    /**
     * Creates a new medical record for a patient.
     * This endpoint is mandatory according to the Kit Commun.
     */
    @PostMapping("/patient/{patientId}")
    public ResponseEntity<MedicalRecordDTO> createMedicalRecord(@PathVariable Long patientId) {
        log.info("REST request to create medical record for patient: {}", patientId);
        MedicalRecordDTO created = medicalRecordService.createMedicalRecord(patientId);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * Retrieves a medical record by ID.
     * This endpoint is mandatory according to the Kit Commun.
     * // Security will be reinforced in Subject 3
     */
    @GetMapping("/{id}")
    public ResponseEntity<MedicalRecordDTO> getMedicalRecordById(@PathVariable Long id) {
        log.info("REST request to get medical record: {}", id);
        return medicalRecordService.getMedicalRecordById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieves a medical record by patient ID.
     * // Permissions will be checked in Subject 2
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<MedicalRecordDTO> getMedicalRecordByPatient(@PathVariable Long patientId) {
        log.info("REST request to get medical record for patient: {}", patientId);
        return medicalRecordService.getMedicalRecordByPatientId(patientId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Updates a medical record.
     * This endpoint is mandatory according to the Kit Commun.
     */
    @PutMapping("/{id}")
    public ResponseEntity<MedicalRecordDTO> updateMedicalRecord(
            @PathVariable Long id,
            @Valid @RequestBody MedicalRecordDTO recordDTO) {
        log.info("REST request to update medical record: {}", id);
        MedicalRecordDTO updated = medicalRecordService.updateMedicalRecord(id, recordDTO);
        return ResponseEntity.ok(updated);
    }

    /**
     * Adds a medical entry to a patient's record.
     * // Business logic will be added in the specialized subject
     */
    @PostMapping("/patient/{patientId}/entries")
    public ResponseEntity<MedicalEntryDTO> addEntry(
            @PathVariable Long patientId,
            @Valid @RequestBody MedicalEntryDTO entryDTO) {
        log.info("REST request to add entry for patient: {}", patientId);
        MedicalEntryDTO created = medicalRecordService.addEntry(patientId, entryDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * Gets or creates a medical record for a patient.
     */
    @GetMapping("/patient/{patientId}/ensure")
    public ResponseEntity<MedicalRecordDTO> getOrCreateMedicalRecord(@PathVariable Long patientId) {
        log.info("REST request to get or create medical record for patient: {}", patientId);
        MedicalRecordDTO record = medicalRecordService.getOrCreateMedicalRecord(patientId);
        return ResponseEntity.ok(record);
    }
}

