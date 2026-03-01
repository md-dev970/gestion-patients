package com.hospital.patient.repository;

import com.hospital.patient.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                         PATIENT REPOSITORY                                   ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS INTERFACE EXISTS:                                                  ║
 * ║  Data access layer for Patient entities.                                     ║
 * ║                                                                              ║
 * ║  WHY extend JpaRepository?                                                   ║
 * ║    - Provides CRUD operations automatically                                  ║
 * ║    - Supports pagination and sorting                                         ║
 * ║    - Allows custom query methods                                             ║
 * ║                                                                              ║
 * ║  Students: Add custom query methods based on your specialized needs.         ║
 * ║  // Business logic will be added in the specialized subject                  ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    /**
     * Finds a patient by their national ID.
     * WHY: National ID is a common lookup criterion.
     * 
     * @param nationalId The national ID to search for
     * @return Optional containing the patient if found
     */
    Optional<Patient> findByNationalId(String nationalId);

    /**
     * Finds a patient by their email.
     * 
     * @param email The email to search for
     * @return Optional containing the patient if found
     */
    Optional<Patient> findByEmail(String email);

    /**
     * Searches patients by last name (case-insensitive).
     * WHY: Common search pattern in hospital systems.
     * 
     * @param lastName The last name to search for
     * @return List of matching patients
     */
    List<Patient> findByLastNameContainingIgnoreCase(String lastName);

    /**
     * Searches patients by first name and last name.
     * // Business logic will be added in the specialized subject
     * 
     * @param firstName First name to search
     * @param lastName Last name to search
     * @return List of matching patients
     */
    List<Patient> findByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCase(
            String firstName, String lastName);

    /**
     * Checks if a patient exists with the given national ID.
     * WHY: Useful for validation before creating new patients.
     * 
     * @param nationalId The national ID to check
     * @return true if a patient exists with this ID
     */
    boolean existsByNationalId(String nationalId);

    /**
     * Custom query example: Find patients by blood type.
     * Students: This shows how to write custom JPQL queries.
     * 
     * @param bloodType The blood type to search for
     * @return List of patients with the specified blood type
     */
    @Query("SELECT p FROM Patient p WHERE p.bloodType = :bloodType")
    List<Patient> findByBloodType(@Param("bloodType") String bloodType);

    /**
     * T1.18: Find patients whose retention period has expired.
     */
    List<Patient> findByRetentionUntilBefore(LocalDate date);
}

