package com.hospital.medicalrecord.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                         MEDICAL RECORD ENTITY                                ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS CLASS EXISTS:                                                      ║
 * ║  Represents a patient's medical dossier (dossier médical).                   ║
 * ║  Contains the complete medical history for a patient.                        ║
 * ║                                                                              ║
 * ║  STRUCTURE:                                                                  ║
 * ║    MedicalRecord (1) ──── (*) MedicalEntry                                   ║
 * ║    One record per patient, multiple entries over time.                       ║
 * ║                                                                              ║
 * ║  // Security will be reinforced in Subject 3                                 ║
 * ║  Students: Implement proper access control and audit logging.                ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
@Entity
@Table(name = "medical_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Reference to the patient (from Patient Service).
     * One medical record per patient.
     */
    @Column(name = "patient_id", unique = true, nullable = false)
    private Long patientId;

    /**
     * Known allergies.
     * // Business logic will be added in the specialized subject
     */
    @Column(length = 1000)
    private String allergies;

    /**
     * Current medications.
     */
    @Column(name = "current_medications", length = 1000)
    private String currentMedications;

    /**
     * Chronic conditions.
     */
    @Column(name = "chronic_conditions", length = 1000)
    private String chronicConditions;

    /**
     * Family medical history.
     */
    @Column(name = "family_history", length = 2000)
    private String familyHistory;

    /**
     * Collection of medical entries (consultations, diagnoses, etc.)
     * // Business logic will be added in the specialized subject
     */
    @OneToMany(mappedBy = "medicalRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MedicalEntry> entries = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Helper method to add an entry.
     */
    public void addEntry(MedicalEntry entry) {
        entries.add(entry);
        entry.setMedicalRecord(this);
    }
}

