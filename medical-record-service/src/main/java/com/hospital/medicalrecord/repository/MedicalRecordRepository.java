package com.hospital.medicalrecord.repository;

import com.hospital.medicalrecord.model.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                      MEDICAL RECORD REPOSITORY                               ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS INTERFACE EXISTS:                                                  ║
 * ║  Data access layer for MedicalRecord entities.                               ║
 * ║                                                                              ║
 * ║  Students: Add custom query methods based on your needs.                     ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    /**
     * Finds a medical record by patient ID.
     * WHY: Each patient has exactly one medical record.
     */
    Optional<MedicalRecord> findByPatientId(Long patientId);

    /**
     * Checks if a medical record exists for a patient.
     */
    boolean existsByPatientId(Long patientId);
}

