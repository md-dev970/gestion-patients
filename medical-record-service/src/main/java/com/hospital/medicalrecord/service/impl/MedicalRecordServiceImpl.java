package com.hospital.medicalrecord.service.impl;

import com.hospital.medicalrecord.dto.MedicalEntryDTO;
import com.hospital.medicalrecord.dto.MedicalRecordDTO;
import com.hospital.medicalrecord.exception.MedicalRecordNotFoundException;
import com.hospital.medicalrecord.mapper.MedicalEntryMapper;
import com.hospital.medicalrecord.mapper.MedicalRecordMapper;
import com.hospital.medicalrecord.model.MedicalEntry;
import com.hospital.medicalrecord.model.MedicalRecord;
import com.hospital.medicalrecord.repository.MedicalEntryRepository;
import com.hospital.medicalrecord.repository.MedicalRecordRepository;
import com.hospital.medicalrecord.service.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                  MEDICAL RECORD SERVICE IMPLEMENTATION                       ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS CLASS EXISTS:                                                      ║
 * ║  Implements business logic for medical record operations.                    ║
 * ║                                                                              ║
 * ║  // Security will be reinforced in Subject 3                                 ║
 * ║  // Permissions will be checked in Subject 2                                 ║
 * ║  // Business logic will be added in the specialized subject                  ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MedicalRecordServiceImpl implements MedicalRecordService {

    private final MedicalRecordRepository recordRepository;
    private final MedicalEntryRepository entryRepository;
    private final MedicalRecordMapper recordMapper;
    private final MedicalEntryMapper entryMapper;

    @Override
    public MedicalRecordDTO createMedicalRecord(Long patientId) {
        log.info("Creating medical record for patient: {}", patientId);
        // // Permissions will be checked in Subject 2
        
        // TODO: Validate patient exists via Patient Service
        
        MedicalRecord record = MedicalRecord.builder()
                .patientId(patientId)
                .build();

        MedicalRecord saved = recordRepository.save(record);
        log.info("Medical record created with ID: {}", saved.getId());

        return recordMapper.toDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MedicalRecordDTO> getMedicalRecordById(Long id) {
        log.debug("Fetching medical record by ID: {}", id);
        // // Security will be reinforced in Subject 3
        return recordRepository.findById(id)
                .map(recordMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MedicalRecordDTO> getMedicalRecordByPatientId(Long patientId) {
        log.debug("Fetching medical record for patient: {}", patientId);
        // // Permissions will be checked in Subject 2
        return recordRepository.findByPatientId(patientId)
                .map(recordMapper::toDTO);
    }

    @Override
    public MedicalRecordDTO updateMedicalRecord(Long id, MedicalRecordDTO recordDTO) {
        log.info("Updating medical record: {}", id);
        // // Permissions will be checked in Subject 2

        MedicalRecord existing = recordRepository.findById(id)
                .orElseThrow(() -> new MedicalRecordNotFoundException("Medical record not found: " + id));

        recordMapper.updateEntityFromDTO(recordDTO, existing);
        MedicalRecord updated = recordRepository.save(existing);

        return recordMapper.toDTO(updated);
    }

    @Override
    public MedicalEntryDTO addEntry(Long patientId, MedicalEntryDTO entryDTO) {
        log.info("Adding entry to medical record for patient: {}", patientId);
        // // Business logic will be added in the specialized subject

        MedicalRecord record = recordRepository.findByPatientId(patientId)
                .orElseThrow(() -> new MedicalRecordNotFoundException(
                        "Medical record not found for patient: " + patientId));

        MedicalEntry entry = entryMapper.toEntity(entryDTO);
        record.addEntry(entry);

        entryRepository.save(entry);
        log.info("Medical entry added with ID: {}", entry.getId());

        return entryMapper.toDTO(entry);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MedicalEntryDTO> getEntryById(Long entryId) {
        log.debug("Fetching medical entry: {}", entryId);
        return entryRepository.findById(entryId)
                .map(entryMapper::toDTO);
    }

    @Override
    public MedicalRecordDTO getOrCreateMedicalRecord(Long patientId) {
        log.debug("Getting or creating medical record for patient: {}", patientId);
        
        return recordRepository.findByPatientId(patientId)
                .map(recordMapper::toDTO)
                .orElseGet(() -> createMedicalRecord(patientId));
    }
}

