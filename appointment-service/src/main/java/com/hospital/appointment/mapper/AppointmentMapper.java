package com.hospital.appointment.mapper;

import com.hospital.appointment.dto.AppointmentCreateRequest;
import com.hospital.appointment.dto.AppointmentDTO;
import com.hospital.appointment.model.Appointment;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface AppointmentMapper {

    AppointmentDTO toDTO(Appointment appointment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "notes", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Appointment toEntity(AppointmentCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDTO(AppointmentDTO dto, @MappingTarget Appointment appointment);
}

