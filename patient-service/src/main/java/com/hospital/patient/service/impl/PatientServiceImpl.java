package com.hospital.patient.service.impl;

import com.hospital.patient.client.AppointmentClient;
import com.hospital.patient.client.ConsultationClient;
import com.hospital.patient.client.MedicalRecordClient;
import com.hospital.patient.dto.PatientCreateRequest;
import com.hospital.patient.dto.PatientDTO;
import com.hospital.patient.exception.DuplicatePatientException;
import com.hospital.patient.exception.PatientNotFoundException;
import com.hospital.patient.mapper.PatientMapper;
import com.hospital.patient.model.Patient;
import com.hospital.patient.repository.PatientRepository;
import com.hospital.patient.service.PatientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                    PATIENT SERVICE IMPLEMENTATION                            ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS CLASS EXISTS:                                                      ║
 * ║  Implements the business logic for patient operations.                       ║
 * ║                                                                              ║
 * ║  @Transactional: Ensures database operations are atomic                      ║
 * ║  @RequiredArgsConstructor: Lombok generates constructor with final fields    ║
 * ║  @Slf4j: Lombok generates a logger instance                                  ║
 * ║                                                                              ║
 * ║  // Business logic will be added in the specialized subject                  ║
 * ║  Students: This is where you implement your business rules.                  ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;
    private final MedicalRecordClient medicalRecordClient;
    private final ConsultationClient consultationClient;
    private final AppointmentClient appointmentClient;

    @Override
    public PatientDTO createPatient(PatientCreateRequest request) {
        log.info("Creating new patient with national ID: {}", request.getNationalId());

        // Check for duplicate national ID
        // // Business logic will be added in the specialized subject
        if (patientRepository.existsByNationalId(request.getNationalId())) {
            throw new DuplicatePatientException(
                    "Patient with national ID " + request.getNationalId() + " already exists");
        }

        // Map DTO to entity
        Patient patient = patientMapper.toEntity(request);

        // Save and return
        Patient savedPatient = patientRepository.save(patient);
        log.info("Patient created with ID: {}", savedPatient.getId());

        return patientMapper.toDTO(savedPatient);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PatientDTO> getPatientById(Long id) {
        log.debug("Fetching patient by ID: {}", id);
        // Permissions will be checked in Subject 2
        return patientRepository.findById(id)
                .map(patientMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PatientDTO> getPatientByNationalId(String nationalId) {
        log.debug("Fetching patient by national ID: {}", nationalId);
        return patientRepository.findByNationalId(nationalId)
                .map(patientMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientDTO> getAllPatients() {
        log.debug("Fetching all patients");
        // Permissions will be checked in Subject 2
        // TODO: Add pagination for large datasets
        return patientRepository.findAll().stream()
                .map(patientMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientDTO> searchPatients(String searchTerm) {
        log.debug("Searching patients with term: {}", searchTerm);
        // Business logic will be added in the specialized subject
        // TODO: Implement more sophisticated search (e.g., full-text search)
        return patientRepository.findByLastNameContainingIgnoreCase(searchTerm).stream()
                .map(patientMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PatientDTO updatePatient(Long id, PatientDTO patientDTO) {
        log.info("Updating patient with ID: {}", id);
        // Permissions will be checked in Subject 2

        Patient existingPatient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found with ID: " + id));

        // Update fields
        // Business logic will be added in the specialized subject
        patientMapper.updateEntityFromDTO(patientDTO, existingPatient);

        Patient updatedPatient = patientRepository.save(existingPatient);
        log.info("Patient updated successfully: {}", id);

        return patientMapper.toDTO(updatedPatient);
    }

    @Override
    public void deletePatient(Long id) {
        log.info("Deleting patient with ID: {}", id);
        // Permissions will be checked in Subject 2
        // Security will be reinforced in Subject 3

        if (!patientRepository.existsById(id)) {
            throw new PatientNotFoundException("Patient not found with ID: " + id);
        }

        // T6.2: Cascade erasure of patient-related data across microservices
        log.info("T6.2: Deleting medical records, consultations and appointments for patient: {}", id);
        medicalRecordClient.deleteMedicalRecordsByPatientId(id);
        consultationClient.deleteConsultationsByPatientId(id);
        appointmentClient.deleteAppointmentsByPatientId(id);

        // TODO: Consider soft delete instead of hard delete
        // Business logic will be added in the specialized subject
        patientRepository.deleteById(id);
        log.info("Patient deleted successfully: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return patientRepository.existsById(id);
    }
}

