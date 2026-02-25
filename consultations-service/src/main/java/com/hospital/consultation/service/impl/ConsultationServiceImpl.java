package com.hospital.consultation.service.impl;

import com.hospital.consultation.dto.ConsultationCreateRequest;
import com.hospital.consultation.dto.ConsultationDTO;
import com.hospital.consultation.dto.ConsultationUpdateRequest;
import com.hospital.consultation.exception.ConsultationNotFoundException;
import com.hospital.consultation.mapper.ConsultationMapper;
import com.hospital.consultation.model.Consultation;
import com.hospital.consultation.model.ConsultationStatus;
import com.hospital.consultation.audit.SecurityAuditSender;
import com.hospital.consultation.repository.ConsultationRepository;
import com.hospital.consultation.service.ConsultationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Implémentation du service de gestion des consultations.
 * Conforme au Kit Commun - Microservice Consultations.
 * 
 * Fonctionnalités:
 *   - Création d'une consultation
 *   - Consultation de l'historique d'un patient
 *   - Mise à jour d'une consultation
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ConsultationServiceImpl implements ConsultationService {

    private final ConsultationRepository consultationRepository;
    private final ConsultationMapper consultationMapper;

    @Override
    public ConsultationDTO createConsultation(ConsultationCreateRequest request) {
        log.info("Creating consultation for patient ID: {}", request.getPatientId());

        Consultation consultation = consultationMapper.toEntity(request);
        consultation.setStatus(ConsultationStatus.SCHEDULED);
        
        if (request.getConsultationDate() == null) {
            consultation.setConsultationDate(LocalDateTime.now());
        }

        Consultation savedConsultation = consultationRepository.save(consultation);
        log.info("Consultation created with ID: {}", savedConsultation.getConsultationId());

        return consultationMapper.toDTO(savedConsultation);
    }

    @Override
    @Transactional(readOnly = true)
    public ConsultationDTO getConsultationById(UUID consultationId) {
        log.debug("Fetching consultation by ID: {}", consultationId);
        
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new ConsultationNotFoundException(
                        "Consultation not found with ID: " + consultationId));
        
        return consultationMapper.toDTO(consultation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsultationDTO> getPatientHistory(Long patientId) {
        log.debug("Fetching consultation history for patient ID: {}", patientId);
        
        List<Consultation> consultations = consultationRepository
                .findByPatientIdOrderByConsultationDateDesc(patientId);
        
        return consultationMapper.toDTOList(consultations);
    }

    @Override
    public ConsultationDTO updateConsultation(UUID consultationId, ConsultationUpdateRequest request) {
        log.info("Updating consultation with ID: {}", consultationId);

        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new ConsultationNotFoundException(
                        "Consultation not found with ID: " + consultationId));

        consultationMapper.updateEntityFromRequest(request, consultation);
        
        Consultation updatedConsultation = consultationRepository.save(consultation);
        log.info("Consultation updated successfully: {}", consultationId);

        return consultationMapper.toDTO(updatedConsultation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsultationDTO> getAllConsultations() {
        log.debug("Fetching all consultations");
        return consultationMapper.toDTOList(consultationRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsultationDTO> getConsultationsByDoctor(Long userId) {
        log.debug("Fetching consultations for doctor ID: {}", userId);
        
        List<Consultation> consultations = consultationRepository
                .findByUserIdOrderByConsultationDateDesc(userId);
        
        return consultationMapper.toDTOList(consultations);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsultationDTO> getConsultationsByDateRange(LocalDateTime start, LocalDateTime end) {
        log.debug("Fetching consultations between {} and {}", start, end);
        
        List<Consultation> consultations = consultationRepository.findByDateRange(start, end);
        
        return consultationMapper.toDTOList(consultations);
    }

    @Override
    public void deleteConsultation(UUID consultationId) {
        log.info("Deleting consultation with ID: {}", consultationId);

        if (!consultationRepository.existsById(consultationId)) {
            throw new ConsultationNotFoundException("Consultation not found with ID: " + consultationId);
        }

        consultationRepository.deleteById(consultationId);
        log.info("Consultation deleted successfully: {}", consultationId);
    }

    @Override
    public void deleteByPatientId(Long patientId) {
        log.info("Deleting all consultations for patient: {}", patientId);
        int deleted = consultationRepository.deleteByPatientId(patientId);
        if (deleted > 0) {
            securityAuditSender.sendPhiDeleted("CONSULTATION", String.valueOf(patientId));
        }
    }
}
