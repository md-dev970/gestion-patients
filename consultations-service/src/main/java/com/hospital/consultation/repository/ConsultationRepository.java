package com.hospital.consultation.repository;

import com.hospital.consultation.model.Consultation;
import com.hospital.consultation.model.ConsultationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository pour les opérations sur les consultations.
 */
@Repository
public interface ConsultationRepository extends JpaRepository<Consultation, UUID> {

    /**
     * Trouve toutes les consultations d'un patient.
     */
    List<Consultation> findByPatientIdOrderByConsultationDateDesc(Long patientId);

    /**
     * Trouve toutes les consultations d'un médecin.
     */
    List<Consultation> findByUserIdOrderByConsultationDateDesc(Long userId);

    /**
     * Trouve les consultations par statut.
     */
    List<Consultation> findByStatus(ConsultationStatus status);

    /**
     * Trouve les consultations d'un patient avec un statut donné.
     */
    List<Consultation> findByPatientIdAndStatus(Long patientId, ConsultationStatus status);

    /**
     * Trouve les consultations entre deux dates.
     */
    @Query("SELECT c FROM Consultation c WHERE c.consultationDate BETWEEN :start AND :end ORDER BY c.consultationDate")
    List<Consultation> findByDateRange(
            @Param("start") LocalDateTime start, 
            @Param("end") LocalDateTime end);

    /**
     * Trouve les consultations d'un patient entre deux dates.
     */
    @Query("SELECT c FROM Consultation c WHERE c.patientId = :patientId AND c.consultationDate BETWEEN :start AND :end ORDER BY c.consultationDate DESC")
    List<Consultation> findByPatientIdAndDateRange(
            @Param("patientId") Long patientId,
            @Param("start") LocalDateTime start, 
            @Param("end") LocalDateTime end);

    /**
     * Compte les consultations d'un patient.
     */
    long countByPatientId(Long patientId);

    /**
     * Vérifie s'il existe des consultations pour un patient.
     */
    boolean existsByPatientId(Long patientId);

    /**
     * Deletes all consultations for a patient. T6.1: cascade erasure.
     */
    @Modifying
    @Query("DELETE FROM Consultation c WHERE c.patientId = :patientId")
    int deleteByPatientId(@Param("patientId") Long patientId);
}
