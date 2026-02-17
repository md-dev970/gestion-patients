package com.hospital.medicalrecord.mapper;

import com.hospital.medicalrecord.dto.MedicalRecordDTO;
import com.hospital.medicalrecord.model.MedicalRecord;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {MedicalEntryMapper.class})
public interface MedicalRecordMapper {

    MedicalRecordDTO toDTO(MedicalRecord record);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "entries", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    MedicalRecord toEntity(MedicalRecordDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "patientId", ignore = true)
    @Mapping(target = "entries", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDTO(MedicalRecordDTO dto, @MappingTarget MedicalRecord record);
}

