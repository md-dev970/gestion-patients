package com.hospital.medicalrecord.service.impl;

import com.hospital.medicalrecord.dto.MedicalEntryDTO;
import com.hospital.medicalrecord.dto.MedicalRecordDTO;
import com.hospital.medicalrecord.exception.MedicalRecordNotFoundException;
import com.hospital.medicalrecord.mapper.MedicalEntryMapper;
import com.hospital.medicalrecord.mapper.MedicalRecordMapper;
import com.hospital.medicalrecord.model.EntryType;
import com.hospital.medicalrecord.model.MedicalEntry;
import com.hospital.medicalrecord.model.MedicalRecord;
import com.hospital.medicalrecord.audit.SecurityAuditSender;
import com.hospital.medicalrecord.repository.MedicalEntryRepository;
import com.hospital.medicalrecord.repository.MedicalRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MedicalRecordServiceImpl Unit Tests")
class MedicalRecordServiceImplTest {

    @Mock
    private MedicalRecordRepository recordRepository;

    @Mock
    private MedicalEntryRepository entryRepository;

    @Mock
    private MedicalRecordMapper recordMapper;

    @Mock
    private MedicalEntryMapper entryMapper;

    @Mock
    private SecurityAuditSender securityAuditSender;

    @InjectMocks
    private MedicalRecordServiceImpl medicalRecordService;

    private MedicalRecord medicalRecord;
    private MedicalRecordDTO medicalRecordDTO;
    private MedicalEntry medicalEntry;
    private MedicalEntryDTO medicalEntryDTO;

    @BeforeEach
    void setUp() {
        medicalRecord = MedicalRecord.builder()
                .id(1L)
                .patientId(100L)
                .build();

        medicalRecordDTO = MedicalRecordDTO.builder()
                .id(1L)
                .patientId(100L)
                .build();

        medicalEntry = MedicalEntry.builder()
                .id(1L)
                .entryType(EntryType.DIAGNOSIS)
                .diagnosis("Patient diagnosed with condition")
                .entryDate(LocalDateTime.now())
                .doctorId(2L)
                .build();

        medicalEntryDTO = MedicalEntryDTO.builder()
                .id(1L)
                .entryType(EntryType.DIAGNOSIS)
                .diagnosis("Patient diagnosed with condition")
                .entryDate(LocalDateTime.now())
                .doctorId(2L)
                .build();
    }

    @Test
    @DisplayName("createMedicalRecord - valid patient ID - returns MedicalRecordDTO")
    void createMedicalRecord_validPatientId_returnsMedicalRecordDTO() {
        // Given
        when(recordRepository.save(any(MedicalRecord.class))).thenReturn(medicalRecord);
        when(recordMapper.toDTO(medicalRecord)).thenReturn(medicalRecordDTO);

        // When
        MedicalRecordDTO result = medicalRecordService.createMedicalRecord(100L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPatientId()).isEqualTo(100L);
        verify(recordRepository).save(any(MedicalRecord.class));
        verify(recordMapper).toDTO(medicalRecord);
    }

    @Test
    @DisplayName("getMedicalRecordById - record found - returns Optional with MedicalRecordDTO")
    void getMedicalRecordById_recordFound_returnsOptionalWithMedicalRecordDTO() {
        // Given
        when(recordRepository.findById(1L)).thenReturn(Optional.of(medicalRecord));
        when(recordMapper.toDTO(medicalRecord)).thenReturn(medicalRecordDTO);

        // When
        Optional<MedicalRecordDTO> result = medicalRecordService.getMedicalRecordById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        verify(recordRepository).findById(1L);
        verify(recordMapper).toDTO(medicalRecord);
    }

    @Test
    @DisplayName("getMedicalRecordById - record not found - returns empty Optional")
    void getMedicalRecordById_recordNotFound_returnsEmptyOptional() {
        // Given
        when(recordRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        Optional<MedicalRecordDTO> result = medicalRecordService.getMedicalRecordById(1L);

        // Then
        assertThat(result).isEmpty();
        verify(recordRepository).findById(1L);
        verify(recordMapper, never()).toDTO(any());
    }

    @Test
    @DisplayName("getMedicalRecordByPatientId - record found - returns Optional with MedicalRecordDTO")
    void getMedicalRecordByPatientId_recordFound_returnsOptionalWithMedicalRecordDTO() {
        // Given
        when(recordRepository.findByPatientId(100L)).thenReturn(Optional.of(medicalRecord));
        when(recordMapper.toDTO(medicalRecord)).thenReturn(medicalRecordDTO);

        // When
        Optional<MedicalRecordDTO> result = medicalRecordService.getMedicalRecordByPatientId(100L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getPatientId()).isEqualTo(100L);
        verify(recordRepository).findByPatientId(100L);
        verify(recordMapper).toDTO(medicalRecord);
    }

    @Test
    @DisplayName("getMedicalRecordByPatientId - record not found - returns empty Optional")
    void getMedicalRecordByPatientId_recordNotFound_returnsEmptyOptional() {
        // Given
        when(recordRepository.findByPatientId(100L)).thenReturn(Optional.empty());

        // When
        Optional<MedicalRecordDTO> result = medicalRecordService.getMedicalRecordByPatientId(100L);

        // Then
        assertThat(result).isEmpty();
        verify(recordRepository).findByPatientId(100L);
        verify(recordMapper, never()).toDTO(any());
    }

    @Test
    @DisplayName("updateMedicalRecord - record found - returns updated MedicalRecordDTO")
    void updateMedicalRecord_recordFound_returnsUpdatedMedicalRecordDTO() {
        // Given
        MedicalRecordDTO updateDTO = MedicalRecordDTO.builder()
                .id(1L)
                .patientId(100L)
                .build();

        when(recordRepository.findById(1L)).thenReturn(Optional.of(medicalRecord));
        when(recordRepository.save(medicalRecord)).thenReturn(medicalRecord);
        when(recordMapper.toDTO(medicalRecord)).thenReturn(updateDTO);

        // When
        MedicalRecordDTO result = medicalRecordService.updateMedicalRecord(1L, updateDTO);

        // Then
        assertThat(result).isNotNull();
        verify(recordRepository).findById(1L);
        verify(recordMapper).updateEntityFromDTO(updateDTO, medicalRecord);
        verify(recordRepository).save(medicalRecord);
        verify(recordMapper).toDTO(medicalRecord);
    }

    @Test
    @DisplayName("updateMedicalRecord - record not found - throws MedicalRecordNotFoundException")
    void updateMedicalRecord_recordNotFound_throwsMedicalRecordNotFoundException() {
        // Given
        MedicalRecordDTO updateDTO = MedicalRecordDTO.builder().id(1L).build();
        when(recordRepository.findById(1L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> medicalRecordService.updateMedicalRecord(1L, updateDTO))
                .isInstanceOf(MedicalRecordNotFoundException.class)
                .hasMessageContaining("Medical record not found");
        verify(recordRepository).findById(1L);
        verify(recordRepository, never()).save(any());
    }

    @Test
    @DisplayName("addEntry - record found - returns MedicalEntryDTO")
    void addEntry_recordFound_returnsMedicalEntryDTO() {
        // Given
        when(recordRepository.findByPatientId(100L)).thenReturn(Optional.of(medicalRecord));
        when(entryMapper.toEntity(medicalEntryDTO)).thenReturn(medicalEntry);
        when(entryRepository.save(medicalEntry)).thenReturn(medicalEntry);
        when(entryMapper.toDTO(medicalEntry)).thenReturn(medicalEntryDTO);

        // When
        MedicalEntryDTO result = medicalRecordService.addEntry(100L, medicalEntryDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(recordRepository).findByPatientId(100L);
        verify(entryMapper).toEntity(medicalEntryDTO);
        verify(entryRepository).save(medicalEntry);
        verify(entryMapper).toDTO(medicalEntry);
    }

    @Test
    @DisplayName("addEntry - record not found - throws MedicalRecordNotFoundException")
    void addEntry_recordNotFound_throwsMedicalRecordNotFoundException() {
        // Given
        when(recordRepository.findByPatientId(100L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> medicalRecordService.addEntry(100L, medicalEntryDTO))
                .isInstanceOf(MedicalRecordNotFoundException.class)
                .hasMessageContaining("Medical record not found");
        verify(recordRepository).findByPatientId(100L);
        verify(entryMapper, never()).toEntity(any());
        verify(entryRepository, never()).save(any());
    }

    @Test
    @DisplayName("getEntryById - entry found - returns Optional with MedicalEntryDTO")
    void getEntryById_entryFound_returnsOptionalWithMedicalEntryDTO() {
        // Given
        when(entryRepository.findById(1L)).thenReturn(Optional.of(medicalEntry));
        when(entryMapper.toDTO(medicalEntry)).thenReturn(medicalEntryDTO);

        // When
        Optional<MedicalEntryDTO> result = medicalRecordService.getEntryById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        verify(entryRepository).findById(1L);
        verify(entryMapper).toDTO(medicalEntry);
    }

    @Test
    @DisplayName("getEntryById - entry not found - returns empty Optional")
    void getEntryById_entryNotFound_returnsEmptyOptional() {
        // Given
        when(entryRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        Optional<MedicalEntryDTO> result = medicalRecordService.getEntryById(1L);

        // Then
        assertThat(result).isEmpty();
        verify(entryRepository).findById(1L);
        verify(entryMapper, never()).toDTO(any());
    }

    @Test
    @DisplayName("getOrCreateMedicalRecord - existing record - returns existing MedicalRecordDTO")
    void getOrCreateMedicalRecord_existingRecord_returnsExistingMedicalRecordDTO() {
        // Given
        when(recordRepository.findByPatientId(100L)).thenReturn(Optional.of(medicalRecord));
        when(recordMapper.toDTO(medicalRecord)).thenReturn(medicalRecordDTO);

        // When
        MedicalRecordDTO result = medicalRecordService.getOrCreateMedicalRecord(100L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(recordRepository).findByPatientId(100L);
        verify(recordRepository, never()).save(any());
        verify(recordMapper).toDTO(medicalRecord);
    }

    @Test
    @DisplayName("getOrCreateMedicalRecord - no existing record - creates and returns new MedicalRecordDTO")
    void getOrCreateMedicalRecord_noExistingRecord_createsAndReturnsNewMedicalRecordDTO() {
        // Given
        when(recordRepository.findByPatientId(100L)).thenReturn(Optional.empty());
        when(recordRepository.save(any(MedicalRecord.class))).thenReturn(medicalRecord);
        when(recordMapper.toDTO(medicalRecord)).thenReturn(medicalRecordDTO);

        // When
        MedicalRecordDTO result = medicalRecordService.getOrCreateMedicalRecord(100L);

        // Then
        assertThat(result).isNotNull();
        verify(recordRepository).findByPatientId(100L);
        verify(recordRepository).save(any(MedicalRecord.class));
        verify(recordMapper).toDTO(medicalRecord);
    }

    @Test
    @DisplayName("deleteByPatientId - record exists - deletes record and sends PHI_DELETED")
    void deleteByPatientId_recordExists_deletesRecordAndSendsPhiDeleted() {
        when(recordRepository.findByPatientId(100L)).thenReturn(Optional.of(medicalRecord));
        doNothing().when(recordRepository).delete(medicalRecord);

        medicalRecordService.deleteByPatientId(100L);

        verify(recordRepository).findByPatientId(100L);
        verify(recordRepository).delete(medicalRecord);
        verify(securityAuditSender).sendPhiDeleted("MEDICAL_RECORD", "100");
    }

    @Test
    @DisplayName("deleteByPatientId - no record - does nothing and does not send PHI_DELETED")
    void deleteByPatientId_noRecord_doesNothingAndNoAudit() {
        when(recordRepository.findByPatientId(100L)).thenReturn(Optional.empty());

        medicalRecordService.deleteByPatientId(100L);

        verify(recordRepository).findByPatientId(100L);
        verify(recordRepository, never()).delete(any());
        verify(securityAuditSender, never()).sendPhiDeleted(any(), any());
    }
}

