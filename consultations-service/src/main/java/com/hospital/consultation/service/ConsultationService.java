package com.hospital.consultation.service;

import com.hospital.consultation.dto.ConsultationCreateRequest;
import com.hospital.consultation.dto.ConsultationDTO;
import com.hospital.consultation.dto.ConsultationUpdateRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Interface du service de gestion des consultations.
 * Conforme au Kit Commun.
 */
public interface ConsultationService {

    /**
     * Création d'une consultation.
     * Scénario 2 du Kit Commun: Médecin authentifié -> POST /consultations
     */
    ConsultationDTO createConsultation(ConsultationCreateRequest request);

    /**
     * Consultation d'une consultation par ID.
     */
    ConsultationDTO getConsultationById(UUID consultationId);

    /**
     * Consultation de l'historique d'un patient.
     * Retourne toutes les consultations d'un patient.
     */
    List<ConsultationDTO> getPatientHistory(Long patientId);

    /**
     * Mise à jour d'une consultation.
     */
    ConsultationDTO updateConsultation(UUID consultationId, ConsultationUpdateRequest request);

    /**
     * Liste toutes les consultations.
     */
    List<ConsultationDTO> getAllConsultations();

    /**
     * Trouve les consultations d'un médecin.
     */
    List<ConsultationDTO> getConsultationsByDoctor(Long userId);

    /**
     * Trouve les consultations dans une plage de dates.
     */
    List<ConsultationDTO> getConsultationsByDateRange(LocalDateTime start, LocalDateTime end);

    /**
     * Supprime une consultation.
     */
    void deleteConsultation(UUID consultationId);

    /**
     * Deletes all consultations for a patient. Idempotent. T6.1: RBAC ADMIN at gateway.
     */
    void deleteByPatientId(Long patientId);
}
