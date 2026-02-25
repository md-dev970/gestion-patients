package com.hospital.medicalrecord.mapper;

import com.hospital.medicalrecord.dto.MedicalEntryDTO;
import com.hospital.medicalrecord.model.MedicalEntry;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-25T17:56:12+0000",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.18 (OpenLogic)"
)
@Component
public class MedicalEntryMapperImpl implements MedicalEntryMapper {

    @Override
    public MedicalEntryDTO toDTO(MedicalEntry entry) {
        if ( entry == null ) {
            return null;
        }

        MedicalEntryDTO.MedicalEntryDTOBuilder medicalEntryDTO = MedicalEntryDTO.builder();

        medicalEntryDTO.id( entry.getId() );
        medicalEntryDTO.entryType( entry.getEntryType() );
        medicalEntryDTO.entryDate( entry.getEntryDate() );
        medicalEntryDTO.doctorId( entry.getDoctorId() );
        medicalEntryDTO.diagnosis( entry.getDiagnosis() );
        medicalEntryDTO.symptoms( entry.getSymptoms() );
        medicalEntryDTO.treatment( entry.getTreatment() );
        medicalEntryDTO.prescription( entry.getPrescription() );
        medicalEntryDTO.notes( entry.getNotes() );
        medicalEntryDTO.followUp( entry.getFollowUp() );

        return medicalEntryDTO.build();
    }

    @Override
    public MedicalEntry toEntity(MedicalEntryDTO dto) {
        if ( dto == null ) {
            return null;
        }

        MedicalEntry.MedicalEntryBuilder medicalEntry = MedicalEntry.builder();

        medicalEntry.entryType( dto.getEntryType() );
        medicalEntry.entryDate( dto.getEntryDate() );
        medicalEntry.doctorId( dto.getDoctorId() );
        medicalEntry.diagnosis( dto.getDiagnosis() );
        medicalEntry.symptoms( dto.getSymptoms() );
        medicalEntry.treatment( dto.getTreatment() );
        medicalEntry.prescription( dto.getPrescription() );
        medicalEntry.notes( dto.getNotes() );
        medicalEntry.followUp( dto.getFollowUp() );

        return medicalEntry.build();
    }
}
