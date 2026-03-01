package com.hospital.patient.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                           PATIENT ENTITY                                     ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS CLASS EXISTS:                                                      ║
 * ║  Represents a patient in the hospital system.                                ║
 * ║  This is the core domain entity for the Patient Service.                     ║
 * ║                                                                              ║
 * ║  Students: Add additional fields based on your specialized subject.          ║
 * ║  The fields here are the MINIMUM required by the Kit Commun.                 ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
@Entity
@Table(name = "patients")
@Data                   // Lombok: Generates getters, setters, toString, equals, hashCode
@Builder               // Lombok: Enables builder pattern for object creation
@NoArgsConstructor     // Lombok: Generates no-args constructor (required by JPA)
@AllArgsConstructor    // Lombok: Generates all-args constructor
public class Patient {

    /**
     * Unique identifier for the patient.
     * WHY UUID: Avoids exposing sequential IDs and allows distributed ID generation.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * National patient identifier (e.g., Social Security Number).
     * WHY unique: Each patient must have a unique national ID.
     * // Business logic will be added in the specialized subject
     */
    @Column(name = "national_id", unique = true, nullable = false)
    private String nationalId;

    /**
     * Patient's first name.
     * This field is mandatory according to the Kit Commun.
     */
    @Column(name = "first_name", nullable = false)
    private String firstName;

    /**
     * Patient's last name.
     * This field is mandatory according to the Kit Commun.
     */
    @Column(name = "last_name", nullable = false)
    private String lastName;

    /**
     * Patient's date of birth.
     * WHY LocalDate: We only need the date, not the time.
     */
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    /**
     * Patient's gender.
     * // Business logic will be added in the specialized subject
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    /**
     * Patient's email address.
     */
    @Column(unique = true)
    private String email;

    /**
     * Patient's phone number.
     */
    @Column(name = "phone_number")
    private String phoneNumber;

    /**
     * Patient's address.
     */
    @Column(length = 500)
    private String address;

    /**
     * Blood type of the patient.
     * // Business logic will be added in the specialized subject
     */
    @Column(name = "blood_type")
    private String bloodType;

    /**
     * Emergency contact name.
     */
    @Column(name = "emergency_contact_name")
    private String emergencyContactName;

    /**
     * Emergency contact phone.
     */
    @Column(name = "emergency_contact_phone")
    private String emergencyContactPhone;

    /**
     * Timestamp when the patient record was created.
     * WHY: Audit trail for data tracking.
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the patient record was last updated.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * End of retention period (T1.17). Data may be purged after this date.
     */
    @Column(name = "retention_until")
    private LocalDate retentionUntil;

    /**
     * T1.19: Whether the patient has given consent for data processing.
     */
    @Column(name = "consent_given", nullable = false)
    @Builder.Default
    private boolean consentGiven = true;

    /**
     * T1.19: Legal basis for processing (e.g. consent, legitimate interest).
     */
    @Column(name = "legal_basis", length = 100)
    private String legalBasis;

    /**
     * Automatically sets timestamps before persisting.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * Automatically updates timestamp before updating.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

