package com.hospital.medicalrecord.mapper;

import com.hospital.medicalrecord.dto.MedicalEntryDTO;
import com.hospital.medicalrecord.model.MedicalEntry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MedicalEntryMapper {

    MedicalEntryDTO toDTO(MedicalEntry entry);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "medicalRecord", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    MedicalEntry toEntity(MedicalEntryDTO dto);
}

