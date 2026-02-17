package com.hospital.consultation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité Consultation - Conforme au Kit Commun.
 * 
 * Données gérées:
 *   - consultation_id (UUID)
 *   - patient_id
 *   - user_id (médecin)
 *   - date
 *   - type de consultation
 *   - diagnostic (champ texte simple)
 */
@Entity
@Table(name = "consultations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Consultation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "consultation_id")
    private UUID consultationId;

    /**
     * Référence au patient (depuis le Patient Service).
     */
    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    /**
     * Référence à l'utilisateur/médecin (depuis le Auth/Users Service).
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * Date et heure de la consultation.
     */
    @Column(name = "consultation_date", nullable = false)
    private LocalDateTime consultationDate;

    /**
     * Type de consultation.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "consultation_type", nullable = false)
    private ConsultationType consultationType;

    /**
     * Diagnostic (champ texte simple).
     * Note: Les données sensibles ne doivent pas apparaître dans les logs ou URLs.
     */
    @Column(columnDefinition = "TEXT")
    private String diagnostic;

    /**
     * Notes additionnelles.
     */
    @Column(columnDefinition = "TEXT")
    private String notes;

    /**
     * Motif de la consultation.
     */
    @Column(length = 500)
    private String motif;

    /**
     * Prescriptions médicales.
     */
    @Column(columnDefinition = "TEXT")
    private String prescriptions;

    /**
     * Statut de la consultation.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ConsultationStatus status = ConsultationStatus.SCHEDULED;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (consultationDate == null) {
            consultationDate = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
