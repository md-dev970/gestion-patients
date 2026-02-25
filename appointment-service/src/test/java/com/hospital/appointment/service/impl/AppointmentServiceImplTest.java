package com.hospital.appointment.service.impl;

import com.hospital.appointment.client.PatientClient;
import com.hospital.appointment.client.StaffClient;
import com.hospital.appointment.dto.AppointmentCreateRequest;
import com.hospital.appointment.dto.AppointmentDTO;
import com.hospital.appointment.exception.AppointmentNotFoundException;
import com.hospital.appointment.exception.InvalidAppointmentException;
import com.hospital.appointment.mapper.AppointmentMapper;
import com.hospital.appointment.model.Appointment;
import com.hospital.appointment.model.AppointmentStatus;
import com.hospital.appointment.model.AppointmentType;
import com.hospital.appointment.repository.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AppointmentServiceImpl Unit Tests")
class AppointmentServiceImplTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AppointmentMapper appointmentMapper;

    @Mock
    private PatientClient patientClient;

    @Mock
    private StaffClient staffClient;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    private AppointmentCreateRequest createRequest;
    private Appointment appointment;
    private AppointmentDTO appointmentDTO;
    private LocalDateTime appointmentDateTime;

    @BeforeEach
    void setUp() {
        appointmentDateTime = LocalDateTime.now().plusDays(1);

        createRequest = AppointmentCreateRequest.builder()
                .patientId(1L)
                .doctorId(2L)
                .appointmentDateTime(appointmentDateTime)
                .durationMinutes(30)
                .appointmentType(AppointmentType.ROUTINE_CHECKUP)
                .reason("Regular checkup")
                .build();

        appointment = Appointment.builder()
                .id(1L)
                .patientId(1L)
                .doctorId(2L)
                .appointmentDateTime(appointmentDateTime)
                .durationMinutes(30)
                .status(AppointmentStatus.SCHEDULED)
                .appointmentType(AppointmentType.ROUTINE_CHECKUP)
                .reason("Regular checkup")
                .build();

        appointmentDTO = AppointmentDTO.builder()
                .id(1L)
                .patientId(1L)
                .doctorId(2L)
                .appointmentDateTime(appointmentDateTime)
                .durationMinutes(30)
                .status(AppointmentStatus.SCHEDULED)
                .appointmentType(AppointmentType.ROUTINE_CHECKUP)
                .reason("Regular checkup")
                .build();
    }

    @Test
    @DisplayName("createAppointment - valid request - returns AppointmentDTO")
    void createAppointment_validRequest_returnsAppointmentDTO() {
        // Given
        when(patientClient.checkPatientExists(1L)).thenReturn(true);
        when(staffClient.checkStaffExists(2L)).thenReturn(true);
        when(appointmentRepository.findConflictingAppointments(eq(2L), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(appointmentMapper.toEntity(createRequest)).thenReturn(appointment);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
        when(appointmentMapper.toDTO(appointment)).thenReturn(appointmentDTO);

        // When
        AppointmentDTO result = appointmentService.createAppointment(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
        verify(patientClient).checkPatientExists(1L);
        verify(staffClient).checkStaffExists(2L);
        verify(appointmentRepository).findConflictingAppointments(eq(2L), any(LocalDateTime.class));
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    @DisplayName("createAppointment - patient not found - throws InvalidAppointmentException")
    void createAppointment_patientNotFound_throwsInvalidAppointmentException() {
        // Given
        when(patientClient.checkPatientExists(1L)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> appointmentService.createAppointment(createRequest))
                .isInstanceOf(InvalidAppointmentException.class)
                .hasMessageContaining("Patient not found");
        verify(patientClient).checkPatientExists(1L);
        verify(staffClient, never()).checkStaffExists(any());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("createAppointment - doctor not found - throws InvalidAppointmentException")
    void createAppointment_doctorNotFound_throwsInvalidAppointmentException() {
        // Given
        when(patientClient.checkPatientExists(1L)).thenReturn(true);
        when(staffClient.checkStaffExists(2L)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> appointmentService.createAppointment(createRequest))
                .isInstanceOf(InvalidAppointmentException.class)
                .hasMessageContaining("Doctor not found");
        verify(patientClient).checkPatientExists(1L);
        verify(staffClient).checkStaffExists(2L);
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("createAppointment - time slot not available - throws InvalidAppointmentException")
    void createAppointment_timeSlotNotAvailable_throwsInvalidAppointmentException() {
        // Given
        when(patientClient.checkPatientExists(1L)).thenReturn(true);
        when(staffClient.checkStaffExists(2L)).thenReturn(true);
        when(appointmentRepository.findConflictingAppointments(eq(2L), any(LocalDateTime.class)))
                .thenReturn(List.of(appointment));

        // When/Then
        assertThatThrownBy(() -> appointmentService.createAppointment(createRequest))
                .isInstanceOf(InvalidAppointmentException.class)
                .hasMessageContaining("Time slot is not available");
        verify(patientClient).checkPatientExists(1L);
        verify(staffClient).checkStaffExists(2L);
        verify(appointmentRepository).findConflictingAppointments(eq(2L), any(LocalDateTime.class));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("getAppointmentById - appointment found - returns Optional with AppointmentDTO")
    void getAppointmentById_appointmentFound_returnsOptionalWithAppointmentDTO() {
        // Given
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentMapper.toDTO(appointment)).thenReturn(appointmentDTO);

        // When
        Optional<AppointmentDTO> result = appointmentService.getAppointmentById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        verify(appointmentRepository).findById(1L);
        verify(appointmentMapper).toDTO(appointment);
    }

    @Test
    @DisplayName("getAppointmentById - appointment not found - returns empty Optional")
    void getAppointmentById_appointmentNotFound_returnsEmptyOptional() {
        // Given
        when(appointmentRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        Optional<AppointmentDTO> result = appointmentService.getAppointmentById(1L);

        // Then
        assertThat(result).isEmpty();
        verify(appointmentRepository).findById(1L);
        verify(appointmentMapper, never()).toDTO(any());
    }

    @Test
    @DisplayName("getAppointmentsByPatient - with results - returns list of AppointmentDTO")
    void getAppointmentsByPatient_withResults_returnsListOfAppointmentDTO() {
        // Given
        List<Appointment> appointments = List.of(appointment);
        when(appointmentRepository.findByPatientIdOrderByAppointmentDateTimeDesc(1L))
                .thenReturn(appointments);
        when(appointmentMapper.toDTO(appointment)).thenReturn(appointmentDTO);

        // When
        List<AppointmentDTO> result = appointmentService.getAppointmentsByPatient(1L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        verify(appointmentRepository).findByPatientIdOrderByAppointmentDateTimeDesc(1L);
    }

    @Test
    @DisplayName("getAppointmentsByPatient - empty results - returns empty list")
    void getAppointmentsByPatient_emptyResults_returnsEmptyList() {
        // Given
        when(appointmentRepository.findByPatientIdOrderByAppointmentDateTimeDesc(1L))
                .thenReturn(Collections.emptyList());

        // When
        List<AppointmentDTO> result = appointmentService.getAppointmentsByPatient(1L);

        // Then
        assertThat(result).isEmpty();
        verify(appointmentRepository).findByPatientIdOrderByAppointmentDateTimeDesc(1L);
    }

    @Test
    @DisplayName("getAppointmentsByDoctor - with results - returns list of AppointmentDTO")
    void getAppointmentsByDoctor_withResults_returnsListOfAppointmentDTO() {
        // Given
        List<Appointment> appointments = List.of(appointment);
        when(appointmentRepository.findByDoctorIdOrderByAppointmentDateTimeAsc(2L))
                .thenReturn(appointments);
        when(appointmentMapper.toDTO(appointment)).thenReturn(appointmentDTO);

        // When
        List<AppointmentDTO> result = appointmentService.getAppointmentsByDoctor(2L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        verify(appointmentRepository).findByDoctorIdOrderByAppointmentDateTimeAsc(2L);
    }

    @Test
    @DisplayName("getAppointmentsByDoctor - empty results - returns empty list")
    void getAppointmentsByDoctor_emptyResults_returnsEmptyList() {
        // Given
        when(appointmentRepository.findByDoctorIdOrderByAppointmentDateTimeAsc(2L))
                .thenReturn(Collections.emptyList());

        // When
        List<AppointmentDTO> result = appointmentService.getAppointmentsByDoctor(2L);

        // Then
        assertThat(result).isEmpty();
        verify(appointmentRepository).findByDoctorIdOrderByAppointmentDateTimeAsc(2L);
    }

    @Test
    @DisplayName("getDoctorAppointmentsForDate - with results - returns filtered list")
    void getDoctorAppointmentsForDate_withResults_returnsFilteredList() {
        // Given
        LocalDate date = LocalDate.now().plusDays(1);
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        List<Appointment> appointments = List.of(appointment);
        
        when(appointmentRepository.findDoctorAppointmentsInRange(2L, startOfDay, endOfDay))
                .thenReturn(appointments);
        when(appointmentMapper.toDTO(appointment)).thenReturn(appointmentDTO);

        // When
        List<AppointmentDTO> result = appointmentService.getDoctorAppointmentsForDate(2L, date);

        // Then
        assertThat(result).hasSize(1);
        verify(appointmentRepository).findDoctorAppointmentsInRange(2L, startOfDay, endOfDay);
    }

    @Test
    @DisplayName("updateAppointment - appointment found - returns updated AppointmentDTO")
    void updateAppointment_appointmentFound_returnsUpdatedAppointmentDTO() {
        // Given
        AppointmentDTO updateDTO = AppointmentDTO.builder()
                .id(1L)
                .patientId(1L)
                .doctorId(2L)
                .appointmentDateTime(appointmentDateTime)
                .durationMinutes(45)
                .reason("Updated reason")
                .build();

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(appointment)).thenReturn(appointment);
        when(appointmentMapper.toDTO(appointment)).thenReturn(updateDTO);

        // When
        AppointmentDTO result = appointmentService.updateAppointment(1L, updateDTO);

        // Then
        assertThat(result).isNotNull();
        verify(appointmentRepository).findById(1L);
        verify(appointmentMapper).updateEntityFromDTO(updateDTO, appointment);
        verify(appointmentRepository).save(appointment);
    }

    @Test
    @DisplayName("updateAppointment - appointment not found - throws AppointmentNotFoundException")
    void updateAppointment_appointmentNotFound_throwsAppointmentNotFoundException() {
        // Given
        AppointmentDTO updateDTO = AppointmentDTO.builder().id(1L).build();
        when(appointmentRepository.findById(1L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> appointmentService.updateAppointment(1L, updateDTO))
                .isInstanceOf(AppointmentNotFoundException.class)
                .hasMessageContaining("Appointment not found");
        verify(appointmentRepository).findById(1L);
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateAppointmentStatus - appointment found - returns updated AppointmentDTO")
    void updateAppointmentStatus_appointmentFound_returnsUpdatedAppointmentDTO() {
        // Given
        AppointmentStatus newStatus = AppointmentStatus.COMPLETED;
        appointment.setStatus(newStatus);
        appointmentDTO.setStatus(newStatus);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(appointment)).thenReturn(appointment);
        when(appointmentMapper.toDTO(appointment)).thenReturn(appointmentDTO);

        // When
        AppointmentDTO result = appointmentService.updateAppointmentStatus(1L, newStatus);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(newStatus);
        verify(appointmentRepository).findById(1L);
        verify(appointmentRepository).save(appointment);
    }

    @Test
    @DisplayName("updateAppointmentStatus - appointment not found - throws AppointmentNotFoundException")
    void updateAppointmentStatus_appointmentNotFound_throwsAppointmentNotFoundException() {
        // Given
        when(appointmentRepository.findById(1L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> appointmentService.updateAppointmentStatus(1L, AppointmentStatus.COMPLETED))
                .isInstanceOf(AppointmentNotFoundException.class)
                .hasMessageContaining("Appointment not found");
        verify(appointmentRepository).findById(1L);
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("cancelAppointment - appointment found - cancels appointment")
    void cancelAppointment_appointmentFound_cancelsAppointment() {
        // Given
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(appointment)).thenReturn(appointment);

        // When
        appointmentService.cancelAppointment(1L);

        // Then
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
        verify(appointmentRepository).findById(1L);
        verify(appointmentRepository).save(appointment);
    }

    @Test
    @DisplayName("cancelAppointment - appointment not found - throws AppointmentNotFoundException")
    void cancelAppointment_appointmentNotFound_throwsAppointmentNotFoundException() {
        // Given
        when(appointmentRepository.findById(1L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> appointmentService.cancelAppointment(1L))
                .isInstanceOf(AppointmentNotFoundException.class)
                .hasMessageContaining("Appointment not found");
        verify(appointmentRepository).findById(1L);
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("isTimeSlotAvailable - no conflicts - returns true")
    void isTimeSlotAvailable_noConflicts_returnsTrue() {
        // Given
        when(appointmentRepository.findConflictingAppointments(2L, appointmentDateTime))
                .thenReturn(Collections.emptyList());

        // When
        boolean result = appointmentService.isTimeSlotAvailable(2L, appointmentDateTime);

        // Then
        assertThat(result).isTrue();
        verify(appointmentRepository).findConflictingAppointments(2L, appointmentDateTime);
    }

    @Test
    @DisplayName("isTimeSlotAvailable - conflicts exist - returns false")
    void isTimeSlotAvailable_conflictsExist_returnsFalse() {
        // Given
        when(appointmentRepository.findConflictingAppointments(2L, appointmentDateTime))
                .thenReturn(List.of(appointment));

        // When
        boolean result = appointmentService.isTimeSlotAvailable(2L, appointmentDateTime);

        // Then
        assertThat(result).isFalse();
        verify(appointmentRepository).findConflictingAppointments(2L, appointmentDateTime);
    }

    @Test
    @DisplayName("deleteByPatientId - calls repository deleteByPatientId")
    void deleteByPatientId_callsRepository() {
        when(appointmentRepository.deleteByPatientId(100L)).thenReturn(2);

        appointmentService.deleteByPatientId(100L);

        verify(appointmentRepository).deleteByPatientId(100L);
    }

    @Test
    @DisplayName("deleteByPatientId - no appointments - idempotent, no exception")
    void deleteByPatientId_noAppointments_idempotent() {
        when(appointmentRepository.deleteByPatientId(100L)).thenReturn(0);

        appointmentService.deleteByPatientId(100L);

        verify(appointmentRepository).deleteByPatientId(100L);
    }
}


