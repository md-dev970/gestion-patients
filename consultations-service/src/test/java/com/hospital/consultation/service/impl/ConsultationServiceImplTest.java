package com.hospital.consultation.service.impl;

import com.hospital.consultation.dto.ConsultationCreateRequest;
import com.hospital.consultation.dto.ConsultationDTO;
import com.hospital.consultation.dto.ConsultationUpdateRequest;
import com.hospital.consultation.exception.ConsultationNotFoundException;
import com.hospital.consultation.mapper.ConsultationMapper;
import com.hospital.consultation.model.Consultation;
import com.hospital.consultation.model.ConsultationStatus;
import com.hospital.consultation.model.ConsultationType;
import com.hospital.consultation.audit.SecurityAuditSender;
import com.hospital.consultation.repository.ConsultationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConsultationServiceImpl Unit Tests")
class ConsultationServiceImplTest {

    @Mock
    private ConsultationRepository consultationRepository;

    @Mock
    private ConsultationMapper consultationMapper;

    @InjectMocks
    private ConsultationServiceImpl consultationService;

    private ConsultationCreateRequest createRequest;
    private Consultation consultation;
    private ConsultationDTO consultationDTO;
    private UUID consultationId;
    private LocalDateTime consultationDate;

    @BeforeEach
    void setUp() {
        consultationId = UUID.randomUUID();
        consultationDate = LocalDateTime.now().plusDays(1);

        createRequest = ConsultationCreateRequest.builder()
                .patientId(1L)
                .userId(2L)
                .consultationDate(consultationDate)
                .consultationType(ConsultationType.SUIVI)
                .notes("Patient should follow up in 3 days")
                .build();

        consultation = Consultation.builder()
                .consultationId(consultationId)
                .patientId(1L)
                .userId(2L)
                .consultationDate(consultationDate)
                .status(ConsultationStatus.SCHEDULED)
                .diagnostic("Common cold")
                .prescriptions("Rest and fluids")
                .notes("Patient should follow up in 3 days")
                .build();

        consultationDTO = ConsultationDTO.builder()
                .consultationId(consultationId)
                .patientId(1L)
                .userId(2L)
                .consultationDate(consultationDate)
                .status(ConsultationStatus.SCHEDULED)
                .diagnostic("Common cold")
                .prescriptions("Rest and fluids")
                .notes("Patient should follow up in 3 days")
                .build();
    }

    @Test
    @DisplayName("createConsultation - valid request - returns ConsultationDTO")
    void createConsultation_validRequest_returnsConsultationDTO() {
        // Given
        when(consultationMapper.toEntity(createRequest)).thenReturn(consultation);
        when(consultationRepository.save(consultation)).thenReturn(consultation);
        when(consultationMapper.toDTO(consultation)).thenReturn(consultationDTO);

        // When
        ConsultationDTO result = consultationService.createConsultation(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getConsultationId()).isEqualTo(consultationId);
        assertThat(result.getStatus()).isEqualTo(ConsultationStatus.SCHEDULED);
        verify(consultationMapper).toEntity(createRequest);
        verify(consultationRepository).save(consultation);
        verify(consultationMapper).toDTO(consultation);
    }

    @Test
    @DisplayName("createConsultation - null consultation date - sets current date")
    void createConsultation_nullConsultationDate_setsCurrentDate() {
        // Given
        createRequest.setConsultationDate(null);
        consultation.setConsultationDate(LocalDateTime.now());
        when(consultationMapper.toEntity(createRequest)).thenReturn(consultation);
        when(consultationRepository.save(any(Consultation.class))).thenAnswer(invocation -> {
            Consultation saved = invocation.getArgument(0);
            assertThat(saved.getConsultationDate()).isNotNull();
            return saved;
        });
        when(consultationMapper.toDTO(any(Consultation.class))).thenReturn(consultationDTO);

        // When
        consultationService.createConsultation(createRequest);

        // Then
        verify(consultationRepository).save(any(Consultation.class));
    }

    @Test
    @DisplayName("getConsultationById - consultation found - returns ConsultationDTO")
    void getConsultationById_consultationFound_returnsConsultationDTO() {
        // Given
        when(consultationRepository.findById(consultationId)).thenReturn(java.util.Optional.of(consultation));
        when(consultationMapper.toDTO(consultation)).thenReturn(consultationDTO);

        // When
        ConsultationDTO result = consultationService.getConsultationById(consultationId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getConsultationId()).isEqualTo(consultationId);
        verify(consultationRepository).findById(consultationId);
        verify(consultationMapper).toDTO(consultation);
    }

    @Test
    @DisplayName("getConsultationById - consultation not found - throws ConsultationNotFoundException")
    void getConsultationById_consultationNotFound_throwsConsultationNotFoundException() {
        // Given
        when(consultationRepository.findById(consultationId)).thenReturn(java.util.Optional.empty());

        // When/Then
        assertThatThrownBy(() -> consultationService.getConsultationById(consultationId))
                .isInstanceOf(ConsultationNotFoundException.class)
                .hasMessageContaining("Consultation not found");
        verify(consultationRepository).findById(consultationId);
        verify(consultationMapper, never()).toDTO(any());
    }

    @Test
    @DisplayName("getPatientHistory - with results - returns list of ConsultationDTO")
    void getPatientHistory_withResults_returnsListOfConsultationDTO() {
        // Given
        List<Consultation> consultations = List.of(consultation);
        when(consultationRepository.findByPatientIdOrderByConsultationDateDesc(1L))
                .thenReturn(consultations);
        when(consultationMapper.toDTOList(consultations)).thenReturn(List.of(consultationDTO));

        // When
        List<ConsultationDTO> result = consultationService.getPatientHistory(1L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPatientId()).isEqualTo(1L);
        verify(consultationRepository).findByPatientIdOrderByConsultationDateDesc(1L);
        verify(consultationMapper).toDTOList(consultations);
    }

    @Test
    @DisplayName("getPatientHistory - empty results - returns empty list")
    void getPatientHistory_emptyResults_returnsEmptyList() {
        // Given
        when(consultationRepository.findByPatientIdOrderByConsultationDateDesc(1L))
                .thenReturn(Collections.emptyList());
        when(consultationMapper.toDTOList(Collections.emptyList()))
                .thenReturn(Collections.emptyList());

        // When
        List<ConsultationDTO> result = consultationService.getPatientHistory(1L);

        // Then
        assertThat(result).isEmpty();
        verify(consultationRepository).findByPatientIdOrderByConsultationDateDesc(1L);
    }

    @Test
    @DisplayName("updateConsultation - consultation found - returns updated ConsultationDTO")
    void updateConsultation_consultationFound_returnsUpdatedConsultationDTO() {
        // Given
        ConsultationUpdateRequest updateRequest = ConsultationUpdateRequest.builder()
                .diagnostic("Updated diagnosis")
                .prescriptions("Updated prescription")
                .notes("Updated notes")
                .build();

        consultation.setDiagnostic("Updated diagnosis");
        consultationDTO.setDiagnostic("Updated diagnosis");

        when(consultationRepository.findById(consultationId)).thenReturn(java.util.Optional.of(consultation));
        when(consultationRepository.save(consultation)).thenReturn(consultation);
        when(consultationMapper.toDTO(consultation)).thenReturn(consultationDTO);

        // When
        ConsultationDTO result = consultationService.updateConsultation(consultationId, updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(consultationRepository).findById(consultationId);
        verify(consultationMapper).updateEntityFromRequest(updateRequest, consultation);
        verify(consultationRepository).save(consultation);
        verify(consultationMapper).toDTO(consultation);
    }

    @Test
    @DisplayName("updateConsultation - consultation not found - throws ConsultationNotFoundException")
    void updateConsultation_consultationNotFound_throwsConsultationNotFoundException() {
        // Given
        ConsultationUpdateRequest updateRequest = ConsultationUpdateRequest.builder().build();
        when(consultationRepository.findById(consultationId)).thenReturn(java.util.Optional.empty());

        // When/Then
        assertThatThrownBy(() -> consultationService.updateConsultation(consultationId, updateRequest))
                .isInstanceOf(ConsultationNotFoundException.class)
                .hasMessageContaining("Consultation not found");
        verify(consultationRepository).findById(consultationId);
        verify(consultationRepository, never()).save(any());
    }

    @Test
    @DisplayName("getAllConsultations - with results - returns list of ConsultationDTO")
    void getAllConsultations_withResults_returnsListOfConsultationDTO() {
        // Given
        List<Consultation> consultations = List.of(consultation);
        when(consultationRepository.findAll()).thenReturn(consultations);
        when(consultationMapper.toDTOList(consultations)).thenReturn(List.of(consultationDTO));

        // When
        List<ConsultationDTO> result = consultationService.getAllConsultations();

        // Then
        assertThat(result).hasSize(1);
        verify(consultationRepository).findAll();
        verify(consultationMapper).toDTOList(consultations);
    }

    @Test
    @DisplayName("getAllConsultations - empty results - returns empty list")
    void getAllConsultations_emptyResults_returnsEmptyList() {
        // Given
        when(consultationRepository.findAll()).thenReturn(Collections.emptyList());
        when(consultationMapper.toDTOList(Collections.emptyList()))
                .thenReturn(Collections.emptyList());

        // When
        List<ConsultationDTO> result = consultationService.getAllConsultations();

        // Then
        assertThat(result).isEmpty();
        verify(consultationRepository).findAll();
    }

    @Test
    @DisplayName("getConsultationsByDoctor - with results - returns filtered list")
    void getConsultationsByDoctor_withResults_returnsFilteredList() {
        // Given
        List<Consultation> consultations = List.of(consultation);
        when(consultationRepository.findByUserIdOrderByConsultationDateDesc(2L))
                .thenReturn(consultations);
        when(consultationMapper.toDTOList(consultations)).thenReturn(List.of(consultationDTO));

        // When
        List<ConsultationDTO> result = consultationService.getConsultationsByDoctor(2L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(2L);
        verify(consultationRepository).findByUserIdOrderByConsultationDateDesc(2L);
        verify(consultationMapper).toDTOList(consultations);
    }

    @Test
    @DisplayName("getConsultationsByDoctor - empty results - returns empty list")
    void getConsultationsByDoctor_emptyResults_returnsEmptyList() {
        // Given
        when(consultationRepository.findByUserIdOrderByConsultationDateDesc(2L))
                .thenReturn(Collections.emptyList());
        when(consultationMapper.toDTOList(Collections.emptyList()))
                .thenReturn(Collections.emptyList());

        // When
        List<ConsultationDTO> result = consultationService.getConsultationsByDoctor(2L);

        // Then
        assertThat(result).isEmpty();
        verify(consultationRepository).findByUserIdOrderByConsultationDateDesc(2L);
    }

    @Test
    @DisplayName("getConsultationsByDateRange - with results - returns filtered list")
    void getConsultationsByDateRange_withResults_returnsFilteredList() {
        // Given
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().plusDays(7);
        List<Consultation> consultations = List.of(consultation);
        
        when(consultationRepository.findByDateRange(start, end)).thenReturn(consultations);
        when(consultationMapper.toDTOList(consultations)).thenReturn(List.of(consultationDTO));

        // When
        List<ConsultationDTO> result = consultationService.getConsultationsByDateRange(start, end);

        // Then
        assertThat(result).hasSize(1);
        verify(consultationRepository).findByDateRange(start, end);
        verify(consultationMapper).toDTOList(consultations);
    }

    @Test
    @DisplayName("getConsultationsByDateRange - empty results - returns empty list")
    void getConsultationsByDateRange_emptyResults_returnsEmptyList() {
        // Given
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().plusDays(7);
        
        when(consultationRepository.findByDateRange(start, end))
                .thenReturn(Collections.emptyList());
        when(consultationMapper.toDTOList(Collections.emptyList()))
                .thenReturn(Collections.emptyList());

        // When
        List<ConsultationDTO> result = consultationService.getConsultationsByDateRange(start, end);

        // Then
        assertThat(result).isEmpty();
        verify(consultationRepository).findByDateRange(start, end);
    }

    @Test
    @DisplayName("deleteConsultation - consultation found - deletes consultation")
    void deleteConsultation_consultationFound_deletesConsultation() {
        // Given
        when(consultationRepository.existsById(consultationId)).thenReturn(true);
        doNothing().when(consultationRepository).deleteById(consultationId);

        // When
        consultationService.deleteConsultation(consultationId);

        // Then
        verify(consultationRepository).existsById(consultationId);
        verify(consultationRepository).deleteById(consultationId);
    }

    @Test
    @DisplayName("deleteConsultation - consultation not found - throws ConsultationNotFoundException")
    void deleteConsultation_consultationNotFound_throwsConsultationNotFoundException() {
        // Given
        when(consultationRepository.existsById(consultationId)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> consultationService.deleteConsultation(consultationId))
                .isInstanceOf(ConsultationNotFoundException.class)
                .hasMessageContaining("Consultation not found");
        verify(consultationRepository).existsById(consultationId);
        verify(consultationRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("deleteByPatientId - consultations deleted - sends PHI_DELETED")
    void deleteByPatientId_deleted_sendsPhiDeleted() {
        when(consultationRepository.deleteByPatientId(100L)).thenReturn(3);

        consultationService.deleteByPatientId(100L);

        verify(consultationRepository).deleteByPatientId(100L);
        verify(securityAuditSender).sendPhiDeleted("CONSULTATION", "100");
    }

    @Test
    @DisplayName("deleteByPatientId - no consultations - idempotent, does not send PHI_DELETED")
    void deleteByPatientId_noConsultations_idempotentNoAudit() {
        when(consultationRepository.deleteByPatientId(100L)).thenReturn(0);

        consultationService.deleteByPatientId(100L);

        verify(consultationRepository).deleteByPatientId(100L);
        verify(securityAuditSender, never()).sendPhiDeleted(any(), any());
    }
}

