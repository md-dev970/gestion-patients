package com.hospital.consultation.mapper;

import com.hospital.consultation.dto.ConsultationCreateRequest;
import com.hospital.consultation.dto.ConsultationDTO;
import com.hospital.consultation.dto.ConsultationUpdateRequest;
import com.hospital.consultation.model.Consultation;
import org.mapstruct.*;

import java.util.List;

/**
 * Mapper pour convertir entre entités Consultation et DTOs.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ConsultationMapper {

    /**
     * Convertit une entité en DTO.
     */
    ConsultationDTO toDTO(Consultation consultation);

    /**
     * Convertit une liste d'entités en liste de DTOs.
     */
    List<ConsultationDTO> toDTOList(List<Consultation> consultations);

    /**
     * Convertit une requête de création en entité.
     */
    @Mapping(target = "consultationId", ignore = true)
    @Mapping(target = "diagnostic", ignore = true)
    @Mapping(target = "prescriptions", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "retentionUntil", ignore = true)
    Consultation toEntity(ConsultationCreateRequest request);

    /**
     * Met à jour une entité à partir d'une requête de mise à jour.
     */
    @Mapping(target = "consultationId", ignore = true)
    @Mapping(target = "patientId", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(ConsultationUpdateRequest request, @MappingTarget Consultation consultation);
}
