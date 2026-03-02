package com.hospital.staff.mapper;

import com.hospital.staff.dto.StaffDTO;
import com.hospital.staff.model.Staff;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-02T21:41:52+0000",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.18 (OpenLogic)"
)
@Component
public class StaffMapperImpl implements StaffMapper {

    @Override
    public StaffDTO toDTO(Staff staff) {
        if ( staff == null ) {
            return null;
        }

        StaffDTO.StaffDTOBuilder staffDTO = StaffDTO.builder();

        staffDTO.id( staff.getId() );
        staffDTO.employeeId( staff.getEmployeeId() );
        staffDTO.firstName( staff.getFirstName() );
        staffDTO.lastName( staff.getLastName() );
        staffDTO.email( staff.getEmail() );
        staffDTO.phoneNumber( staff.getPhoneNumber() );
        staffDTO.role( staff.getRole() );
        staffDTO.specialty( staff.getSpecialty() );
        staffDTO.department( staff.getDepartment() );
        staffDTO.licenseNumber( staff.getLicenseNumber() );
        staffDTO.hireDate( staff.getHireDate() );
        staffDTO.active( staff.isActive() );

        return staffDTO.build();
    }

    @Override
    public Staff toEntity(StaffDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Staff.StaffBuilder staff = Staff.builder();

        staff.employeeId( dto.getEmployeeId() );
        staff.firstName( dto.getFirstName() );
        staff.lastName( dto.getLastName() );
        staff.email( dto.getEmail() );
        staff.phoneNumber( dto.getPhoneNumber() );
        staff.role( dto.getRole() );
        staff.specialty( dto.getSpecialty() );
        staff.department( dto.getDepartment() );
        staff.licenseNumber( dto.getLicenseNumber() );
        staff.hireDate( dto.getHireDate() );
        staff.active( dto.isActive() );

        return staff.build();
    }

    @Override
    public void updateEntityFromDTO(StaffDTO dto, Staff staff) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getEmployeeId() != null ) {
            staff.setEmployeeId( dto.getEmployeeId() );
        }
        if ( dto.getFirstName() != null ) {
            staff.setFirstName( dto.getFirstName() );
        }
        if ( dto.getLastName() != null ) {
            staff.setLastName( dto.getLastName() );
        }
        if ( dto.getEmail() != null ) {
            staff.setEmail( dto.getEmail() );
        }
        if ( dto.getPhoneNumber() != null ) {
            staff.setPhoneNumber( dto.getPhoneNumber() );
        }
        if ( dto.getRole() != null ) {
            staff.setRole( dto.getRole() );
        }
        if ( dto.getSpecialty() != null ) {
            staff.setSpecialty( dto.getSpecialty() );
        }
        if ( dto.getDepartment() != null ) {
            staff.setDepartment( dto.getDepartment() );
        }
        if ( dto.getLicenseNumber() != null ) {
            staff.setLicenseNumber( dto.getLicenseNumber() );
        }
        if ( dto.getHireDate() != null ) {
            staff.setHireDate( dto.getHireDate() );
        }
        staff.setActive( dto.isActive() );
    }
}
