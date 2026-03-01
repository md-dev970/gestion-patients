package com.hospital.patient.service;

import com.hospital.patient.dto.PatientCreateRequest;
import com.hospital.patient.dto.PatientDTO;
import com.hospital.patient.dto.PatientDossierDTO;

import java.util.List;
import java.util.Optional;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                       PATIENT SERVICE INTERFACE                              ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS INTERFACE EXISTS:                                                  ║
 * ║  Defines the contract for patient-related business operations.               ║
 * ║                                                                              ║
 * ║  WHY use an interface?                                                       ║
 * ║    1. Abstraction: Controller doesn't depend on implementation               ║
 * ║    2. Testability: Easy to mock in unit tests                                ║
 * ║    3. Flexibility: Can swap implementations (e.g., for caching)              ║
 * ║                                                                              ║
 * ║  // Business logic will be added in the specialized subject                  ║
 * ║  Students: Implement the business rules in PatientServiceImpl.               ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public interface PatientService {

    /**
     * Creates a new patient in the system.
     * This endpoint is mandatory according to the Kit Commun.
     * 
     * @param request The patient creation data
     * @return The created patient as DTO
     */
    PatientDTO createPatient(PatientCreateRequest request);

    /**
     * Retrieves a patient by their ID.
     * This endpoint is mandatory according to the Kit Commun.
     * 
     * @param id The patient ID
     * @return Optional containing the patient if found
     */
    Optional<PatientDTO> getPatientById(Long id);

    /**
     * Retrieves a patient by their national ID.
     * 
     * @param nationalId The national ID
     * @return Optional containing the patient if found
     */
    Optional<PatientDTO> getPatientByNationalId(String nationalId);

    /**
     * Retrieves all patients.
     * // Permissions will be checked in Subject 2
     * 
     * @return List of all patients
     */
    List<PatientDTO> getAllPatients();

    /**
     * Searches patients by name.
     * 
     * @param searchTerm The search term
     * @return List of matching patients
     */
    List<PatientDTO> searchPatients(String searchTerm);

    /**
     * Updates an existing patient.
     * This endpoint is mandatory according to the Kit Commun.
     * 
     * @param id The patient ID
     * @param patientDTO The updated patient data
     * @return The updated patient as DTO
     */
    PatientDTO updatePatient(Long id, PatientDTO patientDTO);

    /**
     * Deletes a patient by their ID.
     * // Permissions will be checked in Subject 2
     * // Security will be reinforced in Subject 3
     * 
     * @param id The patient ID
     */
    void deletePatient(Long id);

    /**
     * Checks if a patient exists.
     * 
     * @param id The patient ID
     * @return true if the patient exists
     */
    boolean existsById(Long id);

    /**
     * T6.3: Builds the full GDPR dossier for a patient by aggregating data
     * from medical-record, consultations and appointment services.
     *
     * @param id The patient ID
     * @return Aggregated dossier for the patient
     */
    PatientDossierDTO getPatientDossier(Long id);

    /**
     * T1.19: Withdraws consent for data processing for the patient.
     * Sets consentGiven to false and legalBasis to "withdrawn". No PII in logs.
     *
     * @param id The patient ID
     * @return The updated patient DTO
     */
    PatientDTO withdrawConsent(Long id);
}

