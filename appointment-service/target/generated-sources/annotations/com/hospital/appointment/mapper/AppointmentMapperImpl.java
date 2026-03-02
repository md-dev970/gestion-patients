package com.hospital.appointment.mapper;

import com.hospital.appointment.dto.AppointmentCreateRequest;
import com.hospital.appointment.dto.AppointmentDTO;
import com.hospital.appointment.model.Appointment;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-02T21:42:07+0000",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.18 (OpenLogic)"
)
@Component
public class AppointmentMapperImpl implements AppointmentMapper {

    @Override
    public AppointmentDTO toDTO(Appointment appointment) {
        if ( appointment == null ) {
            return null;
        }

        AppointmentDTO.AppointmentDTOBuilder appointmentDTO = AppointmentDTO.builder();

        appointmentDTO.id( appointment.getId() );
        appointmentDTO.patientId( appointment.getPatientId() );
        appointmentDTO.doctorId( appointment.getDoctorId() );
        appointmentDTO.appointmentDateTime( appointment.getAppointmentDateTime() );
        appointmentDTO.durationMinutes( appointment.getDurationMinutes() );
        appointmentDTO.status( appointment.getStatus() );
        appointmentDTO.appointmentType( appointment.getAppointmentType() );
        appointmentDTO.reason( appointment.getReason() );
        appointmentDTO.notes( appointment.getNotes() );
        appointmentDTO.roomNumber( appointment.getRoomNumber() );

        return appointmentDTO.build();
    }

    @Override
    public Appointment toEntity(AppointmentCreateRequest request) {
        if ( request == null ) {
            return null;
        }

        Appointment.AppointmentBuilder appointment = Appointment.builder();

        appointment.patientId( request.getPatientId() );
        appointment.doctorId( request.getDoctorId() );
        appointment.appointmentDateTime( request.getAppointmentDateTime() );
        appointment.durationMinutes( request.getDurationMinutes() );
        appointment.appointmentType( request.getAppointmentType() );
        appointment.reason( request.getReason() );
        appointment.roomNumber( request.getRoomNumber() );

        return appointment.build();
    }

    @Override
    public void updateEntityFromDTO(AppointmentDTO dto, Appointment appointment) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getPatientId() != null ) {
            appointment.setPatientId( dto.getPatientId() );
        }
        if ( dto.getDoctorId() != null ) {
            appointment.setDoctorId( dto.getDoctorId() );
        }
        if ( dto.getAppointmentDateTime() != null ) {
            appointment.setAppointmentDateTime( dto.getAppointmentDateTime() );
        }
        if ( dto.getDurationMinutes() != null ) {
            appointment.setDurationMinutes( dto.getDurationMinutes() );
        }
        if ( dto.getStatus() != null ) {
            appointment.setStatus( dto.getStatus() );
        }
        if ( dto.getAppointmentType() != null ) {
            appointment.setAppointmentType( dto.getAppointmentType() );
        }
        if ( dto.getReason() != null ) {
            appointment.setReason( dto.getReason() );
        }
        if ( dto.getNotes() != null ) {
            appointment.setNotes( dto.getNotes() );
        }
        if ( dto.getRoomNumber() != null ) {
            appointment.setRoomNumber( dto.getRoomNumber() );
        }
    }
}
