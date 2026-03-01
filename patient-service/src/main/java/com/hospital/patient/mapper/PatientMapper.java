package com.hospital.patient.mapper;

import com.hospital.patient.dto.PatientCreateRequest;
import com.hospital.patient.dto.PatientDTO;
import com.hospital.patient.model.Patient;
import org.mapstruct.*;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                          PATIENT MAPPER                                      ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS INTERFACE EXISTS:                                                  ║
 * ║  Maps between Patient entity and DTOs using MapStruct.                       ║
 * ║                                                                              ║
 * ║  WHY MapStruct?                                                              ║
 * ║    1. Compile-time code generation (no runtime overhead)                     ║
 * ║    2. Type-safe mappings                                                     ║
 * ║    3. Easy to customize                                                      ║
 * ║                                                                              ║
 * ║  Students: MapStruct generates the implementation at compile time.           ║
 * ║  Check target/generated-sources to see the generated code.                   ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
@Mapper(componentModel = "spring")
public interface PatientMapper {

    /**
     * Converts a Patient entity to a PatientDTO.
     * WHY: Used when returning data to the API consumer.
     */
    PatientDTO toDTO(Patient patient);

    /**
     * Converts a PatientDTO to a Patient entity.
     * WHY: Used when receiving data from the API.
     */
    @Mapping(target = "id", ignore = true)           // ID is auto-generated
    @Mapping(target = "createdAt", ignore = true)    // Set by @PrePersist
    @Mapping(target = "updatedAt", ignore = true)    // Set by @PrePersist/@PreUpdate
    @Mapping(target = "retentionUntil", ignore = true)
    Patient toEntity(PatientDTO dto);

    /**
     * Converts a PatientCreateRequest to a Patient entity.
     * WHY: Used when creating a new patient.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "retentionUntil", ignore = true)
    @Mapping(target = "consentGiven", ignore = true)
    @Mapping(target = "legalBasis", ignore = true)
    Patient toEntity(PatientCreateRequest request);

    /**
     * Updates an existing Patient entity from a DTO.
     * WHY: Used for partial updates (PATCH operations).
     * 
     * @NullValuePropertyMappingStrategy.IGNORE: Don't overwrite with null values
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "retentionUntil", ignore = true)
    void updateEntityFromDTO(PatientDTO dto, @MappingTarget Patient patient);
}

