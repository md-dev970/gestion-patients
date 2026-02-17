package com.hospital.staff.mapper;

import com.hospital.staff.dto.StaffDTO;
import com.hospital.staff.model.Staff;
import org.mapstruct.*;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                           STAFF MAPPER                                       ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS INTERFACE EXISTS:                                                  ║
 * ║  Maps between Staff entity and DTOs using MapStruct.                         ║
 * ║                                                                              ║
 * ║  Students: MapStruct generates the implementation at compile time.           ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
@Mapper(componentModel = "spring")
public interface StaffMapper {

    StaffDTO toDTO(Staff staff);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Staff toEntity(StaffDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDTO(StaffDTO dto, @MappingTarget Staff staff);
}

