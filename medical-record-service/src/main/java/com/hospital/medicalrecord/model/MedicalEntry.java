package com.hospital.medicalrecord.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                          MEDICAL ENTRY ENTITY                                ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS CLASS EXISTS:                                                      ║
 * ║  Represents a single medical event/entry in the patient's record.            ║
 * ║  Examples: consultations, diagnoses, procedures, prescriptions.              ║
 * ║                                                                              ║
 * ║  Students: Each type of entry may have different required fields.            ║
 * ║  // Business logic will be added in the specialized subject                  ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
@Entity
@Table(name = "medical_entries")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Parent medical record.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_record_id", nullable = false)
    private MedicalRecord medicalRecord;

    /**
     * Type of medical entry.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false)
    private EntryType entryType;

    /**
     * When this medical event occurred.
     */
    @Column(name = "entry_date", nullable = false)
    private LocalDateTime entryDate;

    /**
     * Reference to the doctor who made the entry.
     */
    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;

    /**
     * Diagnosis (ICD code or description).
     * // Business logic will be added in the specialized subject
     */
    @Column(length = 500)
    private String diagnosis;

    /**
     * Symptoms reported.
     */
    @Column(length = 1000)
    private String symptoms;

    /**
     * Treatment prescribed.
     */
    @Column(length = 1000)
    private String treatment;

    /**
     * Prescription details.
     */
    @Column(length = 1000)
    private String prescription;

    /**
     * Clinical notes.
     * // Security will be reinforced in Subject 3
     */
    @Column(length = 2000)
    private String notes;

    /**
     * Follow-up instructions.
     */
    @Column(name = "follow_up", length = 500)
    private String followUp;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (entryDate == null) {
            entryDate = LocalDateTime.now();
        }
    }
}

