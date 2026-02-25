package com.hospital.patient.mapper;

import com.hospital.patient.dto.PatientCreateRequest;
import com.hospital.patient.dto.PatientDTO;
import com.hospital.patient.model.Patient;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-25T18:15:59+0000",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.18 (OpenLogic)"
)
@Component
public class PatientMapperImpl implements PatientMapper {

    @Override
    public PatientDTO toDTO(Patient patient) {
        if ( patient == null ) {
            return null;
        }

        PatientDTO.PatientDTOBuilder patientDTO = PatientDTO.builder();

        patientDTO.id( patient.getId() );
        patientDTO.nationalId( patient.getNationalId() );
        patientDTO.firstName( patient.getFirstName() );
        patientDTO.lastName( patient.getLastName() );
        patientDTO.dateOfBirth( patient.getDateOfBirth() );
        patientDTO.gender( patient.getGender() );
        patientDTO.email( patient.getEmail() );
        patientDTO.phoneNumber( patient.getPhoneNumber() );
        patientDTO.address( patient.getAddress() );
        patientDTO.bloodType( patient.getBloodType() );
        patientDTO.emergencyContactName( patient.getEmergencyContactName() );
        patientDTO.emergencyContactPhone( patient.getEmergencyContactPhone() );

        return patientDTO.build();
    }

    @Override
    public Patient toEntity(PatientDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Patient.PatientBuilder patient = Patient.builder();

        patient.nationalId( dto.getNationalId() );
        patient.firstName( dto.getFirstName() );
        patient.lastName( dto.getLastName() );
        patient.dateOfBirth( dto.getDateOfBirth() );
        patient.gender( dto.getGender() );
        patient.email( dto.getEmail() );
        patient.phoneNumber( dto.getPhoneNumber() );
        patient.address( dto.getAddress() );
        patient.bloodType( dto.getBloodType() );
        patient.emergencyContactName( dto.getEmergencyContactName() );
        patient.emergencyContactPhone( dto.getEmergencyContactPhone() );

        return patient.build();
    }

    @Override
    public Patient toEntity(PatientCreateRequest request) {
        if ( request == null ) {
            return null;
        }

        Patient.PatientBuilder patient = Patient.builder();

        patient.nationalId( request.getNationalId() );
        patient.firstName( request.getFirstName() );
        patient.lastName( request.getLastName() );
        patient.dateOfBirth( request.getDateOfBirth() );
        patient.gender( request.getGender() );
        patient.email( request.getEmail() );
        patient.phoneNumber( request.getPhoneNumber() );
        patient.address( request.getAddress() );
        patient.bloodType( request.getBloodType() );
        patient.emergencyContactName( request.getEmergencyContactName() );
        patient.emergencyContactPhone( request.getEmergencyContactPhone() );

        return patient.build();
    }

    @Override
    public void updateEntityFromDTO(PatientDTO dto, Patient patient) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getNationalId() != null ) {
            patient.setNationalId( dto.getNationalId() );
        }
        if ( dto.getFirstName() != null ) {
            patient.setFirstName( dto.getFirstName() );
        }
        if ( dto.getLastName() != null ) {
            patient.setLastName( dto.getLastName() );
        }
        if ( dto.getDateOfBirth() != null ) {
            patient.setDateOfBirth( dto.getDateOfBirth() );
        }
        if ( dto.getGender() != null ) {
            patient.setGender( dto.getGender() );
        }
        if ( dto.getEmail() != null ) {
            patient.setEmail( dto.getEmail() );
        }
        if ( dto.getPhoneNumber() != null ) {
            patient.setPhoneNumber( dto.getPhoneNumber() );
        }
        if ( dto.getAddress() != null ) {
            patient.setAddress( dto.getAddress() );
        }
        if ( dto.getBloodType() != null ) {
            patient.setBloodType( dto.getBloodType() );
        }
        if ( dto.getEmergencyContactName() != null ) {
            patient.setEmergencyContactName( dto.getEmergencyContactName() );
        }
        if ( dto.getEmergencyContactPhone() != null ) {
            patient.setEmergencyContactPhone( dto.getEmergencyContactPhone() );
        }
    }
}
