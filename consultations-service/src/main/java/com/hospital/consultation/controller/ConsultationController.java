package com.hospital.consultation.controller;

import com.hospital.consultation.dto.ConsultationCreateRequest;
import com.hospital.consultation.dto.ConsultationDTO;
import com.hospital.consultation.dto.ConsultationUpdateRequest;
import com.hospital.consultation.service.ConsultationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Contrôleur REST pour les consultations médicales.
 * Conforme au Kit Commun - Microservice Consultations.
 * 
 * Endpoints obligatoires:
 *   POST /consultations - Création d'une consultation
 *   GET /consultations/patient/{patientId} - Historique d'un patient
 *   PUT /consultations/{id} - Mise à jour d'une consultation
 */
@RestController
@RequestMapping("/api/consultations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Consultations", description = "API de gestion des consultations médicales")
public class ConsultationController {

    private final ConsultationService consultationService;

    /**
     * Création d'une consultation.
     * Endpoint obligatoire selon le Kit Commun.
     * 
     * Scénario 2: Médecin authentifié -> POST /consultations -> Association patient ↔ consultation
     */
    @PostMapping
    @Operation(summary = "Créer une consultation", description = "Crée une nouvelle consultation médicale")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Consultation créée avec succès"),
            @ApiResponse(responseCode = "400", description = "Requête invalide"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Non autorisé")
    })
    public ResponseEntity<ConsultationDTO> createConsultation(
            @Valid @RequestBody ConsultationCreateRequest request) {
        log.info("REST request to create consultation for patient: {}", request.getPatientId());
        ConsultationDTO consultation = consultationService.createConsultation(request);
        return new ResponseEntity<>(consultation, HttpStatus.CREATED);
    }

    /**
     * Récupère une consultation par son ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtenir une consultation", description = "Récupère une consultation par son ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consultation trouvée"),
            @ApiResponse(responseCode = "404", description = "Consultation non trouvée")
    })
    public ResponseEntity<ConsultationDTO> getConsultation(
            @Parameter(description = "ID de la consultation") @PathVariable UUID id) {
        log.debug("REST request to get consultation: {}", id);
        ConsultationDTO consultation = consultationService.getConsultationById(id);
        return ResponseEntity.ok(consultation);
    }

    /**
     * Consultation de l'historique d'un patient.
     * Endpoint obligatoire selon le Kit Commun.
     */
    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Historique patient", description = "Récupère l'historique des consultations d'un patient")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historique récupéré"),
            @ApiResponse(responseCode = "404", description = "Patient non trouvé")
    })
    public ResponseEntity<List<ConsultationDTO>> getPatientHistory(
            @Parameter(description = "ID du patient") @PathVariable Long patientId) {
        log.debug("REST request to get consultation history for patient: {}", patientId);
        List<ConsultationDTO> history = consultationService.getPatientHistory(patientId);
        return ResponseEntity.ok(history);
    }

    /**
     * Mise à jour d'une consultation.
     * Endpoint obligatoire selon le Kit Commun.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une consultation", description = "Met à jour une consultation existante")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consultation mise à jour"),
            @ApiResponse(responseCode = "400", description = "Requête invalide"),
            @ApiResponse(responseCode = "404", description = "Consultation non trouvée")
    })
    public ResponseEntity<ConsultationDTO> updateConsultation(
            @Parameter(description = "ID de la consultation") @PathVariable UUID id,
            @Valid @RequestBody ConsultationUpdateRequest request) {
        log.info("REST request to update consultation: {}", id);
        ConsultationDTO consultation = consultationService.updateConsultation(id, request);
        return ResponseEntity.ok(consultation);
    }

    /**
     * Liste toutes les consultations.
     */
    @GetMapping
    @Operation(summary = "Lister les consultations", description = "Récupère toutes les consultations")
    public ResponseEntity<List<ConsultationDTO>> getAllConsultations() {
        log.debug("REST request to get all consultations");
        List<ConsultationDTO> consultations = consultationService.getAllConsultations();
        return ResponseEntity.ok(consultations);
    }

    /**
     * Récupère les consultations d'un médecin.
     */
    @GetMapping("/doctor/{userId}")
    @Operation(summary = "Consultations par médecin", description = "Récupère les consultations d'un médecin")
    public ResponseEntity<List<ConsultationDTO>> getConsultationsByDoctor(
            @Parameter(description = "ID du médecin/utilisateur") @PathVariable Long userId) {
        log.debug("REST request to get consultations for doctor: {}", userId);
        List<ConsultationDTO> consultations = consultationService.getConsultationsByDoctor(userId);
        return ResponseEntity.ok(consultations);
    }

    /**
     * Récupère les consultations dans une plage de dates.
     */
    @GetMapping("/date-range")
    @Operation(summary = "Consultations par période", description = "Récupère les consultations dans une plage de dates")
    public ResponseEntity<List<ConsultationDTO>> getConsultationsByDateRange(
            @Parameter(description = "Date de début") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @Parameter(description = "Date de fin") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        log.debug("REST request to get consultations between {} and {}", start, end);
        List<ConsultationDTO> consultations = consultationService.getConsultationsByDateRange(start, end);
        return ResponseEntity.ok(consultations);
    }

    /**
     * Supprime une consultation.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une consultation", description = "Supprime une consultation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Consultation supprimée"),
            @ApiResponse(responseCode = "404", description = "Consultation non trouvée")
    })
    public ResponseEntity<Void> deleteConsultation(
            @Parameter(description = "ID de la consultation") @PathVariable UUID id) {
        log.info("REST request to delete consultation: {}", id);
        consultationService.deleteConsultation(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Deletes all consultations for a patient. Idempotent. T6.1: RBAC ADMIN at gateway.
     */
    @DeleteMapping("/patient/{patientId}")
    @Operation(summary = "Supprimer les consultations d'un patient", description = "Supprime toutes les consultations du patient (effacement en cascade)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Suppression effectuée")
    })
    public ResponseEntity<Void> deleteByPatientId(
            @Parameter(description = "ID du patient") @PathVariable Long patientId) {
        log.info("REST request to delete all consultations for patient: {}", patientId);
        consultationService.deleteByPatientId(patientId);
        return ResponseEntity.noContent().build();
    }
}
