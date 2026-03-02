package com.hospital.medicalrecord.mapper;

import com.hospital.medicalrecord.dto.MedicalEntryDTO;
import com.hospital.medicalrecord.dto.MedicalRecordDTO;
import com.hospital.medicalrecord.model.MedicalEntry;
import com.hospital.medicalrecord.model.MedicalRecord;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-02T21:42:23+0000",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.18 (OpenLogic)"
)
@Component
public class MedicalRecordMapperImpl implements MedicalRecordMapper {

    @Autowired
    private MedicalEntryMapper medicalEntryMapper;

    @Override
    public MedicalRecordDTO toDTO(MedicalRecord record) {
        if ( record == null ) {
            return null;
        }

        MedicalRecordDTO.MedicalRecordDTOBuilder medicalRecordDTO = MedicalRecordDTO.builder();

        medicalRecordDTO.id( record.getId() );
        medicalRecordDTO.patientId( record.getPatientId() );
        medicalRecordDTO.allergies( record.getAllergies() );
        medicalRecordDTO.currentMedications( record.getCurrentMedications() );
        medicalRecordDTO.chronicConditions( record.getChronicConditions() );
        medicalRecordDTO.familyHistory( record.getFamilyHistory() );
        medicalRecordDTO.entries( medicalEntryListToMedicalEntryDTOList( record.getEntries() ) );

        return medicalRecordDTO.build();
    }

    @Override
    public MedicalRecord toEntity(MedicalRecordDTO dto) {
        if ( dto == null ) {
            return null;
        }

        MedicalRecord.MedicalRecordBuilder medicalRecord = MedicalRecord.builder();

        medicalRecord.patientId( dto.getPatientId() );
        medicalRecord.allergies( dto.getAllergies() );
        medicalRecord.currentMedications( dto.getCurrentMedications() );
        medicalRecord.chronicConditions( dto.getChronicConditions() );
        medicalRecord.familyHistory( dto.getFamilyHistory() );

        return medicalRecord.build();
    }

    @Override
    public void updateEntityFromDTO(MedicalRecordDTO dto, MedicalRecord record) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getAllergies() != null ) {
            record.setAllergies( dto.getAllergies() );
        }
        if ( dto.getCurrentMedications() != null ) {
            record.setCurrentMedications( dto.getCurrentMedications() );
        }
        if ( dto.getChronicConditions() != null ) {
            record.setChronicConditions( dto.getChronicConditions() );
        }
        if ( dto.getFamilyHistory() != null ) {
            record.setFamilyHistory( dto.getFamilyHistory() );
        }
    }

    protected List<MedicalEntryDTO> medicalEntryListToMedicalEntryDTOList(List<MedicalEntry> list) {
        if ( list == null ) {
            return null;
        }

        List<MedicalEntryDTO> list1 = new ArrayList<MedicalEntryDTO>( list.size() );
        for ( MedicalEntry medicalEntry : list ) {
            list1.add( medicalEntryMapper.toDTO( medicalEntry ) );
        }

        return list1;
    }
}
