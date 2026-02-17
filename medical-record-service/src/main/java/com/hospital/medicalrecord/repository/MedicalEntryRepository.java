package com.hospital.medicalrecord.repository;

import com.hospital.medicalrecord.model.EntryType;
import com.hospital.medicalrecord.model.MedicalEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                       MEDICAL ENTRY REPOSITORY                               ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS INTERFACE EXISTS:                                                  ║
 * ║  Data access layer for MedicalEntry entities.                                ║
 * ║                                                                              ║
 * ║  // Business logic will be added in the specialized subject                  ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
@Repository
public interface MedicalEntryRepository extends JpaRepository<MedicalEntry, Long> {

    /**
     * Finds entries by type for a medical record.
     */
    List<MedicalEntry> findByMedicalRecordIdAndEntryType(Long recordId, EntryType entryType);

    /**
     * Finds entries by doctor.
     */
    List<MedicalEntry> findByDoctorId(Long doctorId);

    /**
     * Finds entries in a date range.
     */
    @Query("SELECT e FROM MedicalEntry e WHERE e.medicalRecord.patientId = :patientId " +
           "AND e.entryDate BETWEEN :start AND :end ORDER BY e.entryDate DESC")
    List<MedicalEntry> findPatientEntriesInDateRange(
            @Param("patientId") Long patientId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}

