package com.hospital.patient.controller;

import com.hospital.patient.audit.SecurityAuditSender;
import com.hospital.patient.dto.PatientCreateRequest;
import com.hospital.patient.dto.PatientDTO;
import com.hospital.patient.dto.PatientDossierDTO;
import com.hospital.patient.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                         PATIENT REST CONTROLLER                              ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS CLASS EXISTS:                                                      ║
 * ║  Exposes REST API endpoints for patient operations.                          ║
 * ║  This is the entry point for all HTTP requests to the Patient Service.       ║
 * ║                                                                              ║
 * ║  This endpoint is mandatory according to the Kit Commun                      ║
 * ║                                                                              ║
 * ║  Base URL: /api/patients                                                     ║
 * ║                                                                              ║
 * ║  Students: Controllers should be THIN - delegate to services.                ║
 * ║  // Permissions will be checked in Subject 2                                 ║
 * ║  // Security will be reinforced in Subject 3                                 ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
@Slf4j
public class PatientController {

    private final PatientService patientService;
    private final SecurityAuditSender securityAuditSender;

    // ═══════════════════════════════════════════════════════════════════════════
    // CREATE OPERATIONS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Creates a new patient.
     * 
     * This endpoint is mandatory according to the Kit Commun
     * 
     * @param request The patient creation request
     * @return The created patient with HTTP 201
     */
    @PostMapping
    public ResponseEntity<PatientDTO> createPatient(@Valid @RequestBody PatientCreateRequest request) {
        log.info("REST request to create patient");
        // Permissions will be checked in Subject 2
        PatientDTO createdPatient = patientService.createPatient(request);
        return new ResponseEntity<>(createdPatient, HttpStatus.CREATED);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // READ OPERATIONS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Retrieves all patients.
     * 
     * This endpoint is mandatory according to the Kit Commun
     * // Permissions will be checked in Subject 2
     * 
     * @return List of all patients
     */
    @GetMapping
    public ResponseEntity<List<PatientDTO>> getAllPatients() {
        log.info("REST request to get all patients");
        // TODO: Add pagination for production use
        List<PatientDTO> patients = patientService.getAllPatients();
        return ResponseEntity.ok(patients);
    }

    /**
     * Retrieves a patient by ID.
     * 
     * This endpoint is mandatory according to the Kit Commun
     * 
     * @param id The patient ID
     * @return The patient if found, 404 otherwise
     */
    @GetMapping("/{id}")
    public ResponseEntity<PatientDTO> getPatientById(@PathVariable Long id) {
        log.info("REST request to get patient by ID: {}", id);
        // Permissions will be checked in Subject 2
        return patientService.getPatientById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * T6.3: Returns the full GDPR dossier for a patient (aggregated view).
     *
     * @param id The patient ID
     * @return Aggregated dossier with HTTP 200, or 404 if patient not found
     */
    @GetMapping("/{id}/dossier")
    public ResponseEntity<PatientDossierDTO> getPatientDossier(@PathVariable Long id) {
        log.info("REST request to get full dossier for patient: {}", id);
        PatientDossierDTO dossier = patientService.getPatientDossier(id);
        securityAuditSender.sendDossierAccessed(String.valueOf(id), "READ");
        return ResponseEntity.ok(dossier);
    }

    /**
     * T6.6: Exports the full GDPR dossier for a patient as a downloadable JSON file.
     * T6.7: Response must have Content-Type: application/json.
     *
     * @param id The patient ID
     * @return Aggregated dossier with Content-Disposition attachment and Content-Type application/json
     */
    @GetMapping(value = "/{id}/dossier/export", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PatientDossierDTO> exportPatientDossier(@PathVariable Long id) {
        log.info("T6.6: REST request to export full dossier for patient: {}", id);
        PatientDossierDTO dossier = patientService.getPatientDossier(id);
        securityAuditSender.sendDossierAccessed(String.valueOf(id), "EXPORT");

        String filename = "patient-" + id + "-dossier.json";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(dossier);
    }

    /**
     * Retrieves a patient by national ID.
     * 
     * @param nationalId The national ID
     * @return The patient if found, 404 otherwise
     */
    @GetMapping("/national-id/{nationalId}")
    public ResponseEntity<PatientDTO> getPatientByNationalId(@PathVariable String nationalId) {
        log.info("REST request to get patient by national ID: {}", nationalId);
        // Security will be reinforced in Subject 3
        return patientService.getPatientByNationalId(nationalId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Searches patients by name.
     * // Business logic will be added in the specialized subject
     * 
     * @param query The search query
     * @return List of matching patients
     */
    @GetMapping("/search")
    public ResponseEntity<List<PatientDTO>> searchPatients(@RequestParam String query) {
        log.info("REST request to search patients with query: {}", query);
        List<PatientDTO> patients = patientService.searchPatients(query);
        return ResponseEntity.ok(patients);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // UPDATE OPERATIONS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Updates an existing patient.
     * 
     * This endpoint is mandatory according to the Kit Commun
     * // Permissions will be checked in Subject 2
     * 
     * @param id The patient ID
     * @param patientDTO The updated patient data
     * @return The updated patient
     */
    @PutMapping("/{id}")
    public ResponseEntity<PatientDTO> updatePatient(
            @PathVariable Long id,
            @Valid @RequestBody PatientDTO patientDTO) {
        log.info("REST request to update patient: {}", id);
        PatientDTO updatedPatient = patientService.updatePatient(id, patientDTO);
        return ResponseEntity.ok(updatedPatient);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // DELETE OPERATIONS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Deletes a patient by ID.
     * 
     * // Permissions will be checked in Subject 2
     * // Security will be reinforced in Subject 3
     * 
     * @param id The patient ID
     * @return HTTP 204 No Content on success
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        log.info("REST request to delete patient: {}", id);
        // TODO: Consider soft delete for audit trail
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // UTILITY OPERATIONS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Checks if a patient exists.
     * WHY: Useful for other services to validate patient references.
     * 
     * @param id The patient ID
     * @return HTTP 200 if exists, 404 if not
     */
    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> checkPatientExists(@PathVariable Long id) {
        log.debug("REST request to check if patient exists: {}", id);
        boolean exists = patientService.existsById(id);
        return ResponseEntity.ok(exists);
    }
}

