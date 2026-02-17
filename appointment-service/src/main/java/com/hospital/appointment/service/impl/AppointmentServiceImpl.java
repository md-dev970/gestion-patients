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
import com.hospital.appointment.repository.AppointmentRepository;
import com.hospital.appointment.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                   APPOINTMENT SERVICE IMPLEMENTATION                         ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS CLASS EXISTS:                                                      ║
 * ║  Implements the business logic for appointment operations.                   ║
 * ║                                                                              ║
 * ║  INTER-SERVICE COMMUNICATION:                                                ║
 * ║  Uses Feign clients to validate patient/doctor IDs with other services.      ║
 * ║                                                                              ║
 * ║  // Business logic will be added in the specialized subject                  ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;
    private final PatientClient patientClient;
    private final StaffClient staffClient;

    @Override
    public AppointmentDTO createAppointment(AppointmentCreateRequest request) {
        log.info("Creating appointment for patient {} with doctor {}", 
                request.getPatientId(), request.getDoctorId());

        // ═══════════════════════════════════════════════════════════════════════
        // VALIDATE REFERENCES (Inter-service communication)
        // // Business logic will be added in the specialized subject
        // ═══════════════════════════════════════════════════════════════════════
        validatePatientExists(request.getPatientId());
        validateDoctorExists(request.getDoctorId());

        // Check for time slot availability
        // // Business logic will be added in the specialized subject
        if (!isTimeSlotAvailable(request.getDoctorId(), request.getAppointmentDateTime())) {
            throw new InvalidAppointmentException("Time slot is not available");
        }

        Appointment appointment = appointmentMapper.toEntity(request);
        appointment.setStatus(AppointmentStatus.SCHEDULED);

        Appointment savedAppointment = appointmentRepository.save(appointment);
        log.info("Appointment created with ID: {}", savedAppointment.getId());

        return appointmentMapper.toDTO(savedAppointment);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AppointmentDTO> getAppointmentById(Long id) {
        log.debug("Fetching appointment by ID: {}", id);
        return appointmentRepository.findById(id)
                .map(appointmentMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDTO> getAppointmentsByPatient(Long patientId) {
        log.debug("Fetching appointments for patient: {}", patientId);
        return appointmentRepository.findByPatientIdOrderByAppointmentDateTimeDesc(patientId)
                .stream()
                .map(appointmentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDTO> getAppointmentsByDoctor(Long doctorId) {
        log.debug("Fetching appointments for doctor: {}", doctorId);
        return appointmentRepository.findByDoctorIdOrderByAppointmentDateTimeAsc(doctorId)
                .stream()
                .map(appointmentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDTO> getDoctorAppointmentsForDate(Long doctorId, LocalDate date) {
        log.debug("Fetching appointments for doctor {} on date {}", doctorId, date);
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        return appointmentRepository.findDoctorAppointmentsInRange(doctorId, startOfDay, endOfDay)
                .stream()
                .map(appointmentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AppointmentDTO updateAppointment(Long id, AppointmentDTO appointmentDTO) {
        log.info("Updating appointment: {}", id);
        // Permissions will be checked in Subject 2

        Appointment existingAppointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found: " + id));

        appointmentMapper.updateEntityFromDTO(appointmentDTO, existingAppointment);
        Appointment updatedAppointment = appointmentRepository.save(existingAppointment);

        return appointmentMapper.toDTO(updatedAppointment);
    }

    @Override
    public AppointmentDTO updateAppointmentStatus(Long id, AppointmentStatus status) {
        log.info("Updating status for appointment {} to {}", id, status);
        // // Business logic will be added in the specialized subject
        // TODO: Validate status transitions (e.g., can't go from COMPLETED to SCHEDULED)

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found: " + id));

        appointment.setStatus(status);
        Appointment updated = appointmentRepository.save(appointment);

        return appointmentMapper.toDTO(updated);
    }

    @Override
    public void cancelAppointment(Long id) {
        log.info("Cancelling appointment: {}", id);
        // Permissions will be checked in Subject 2

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found: " + id));

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isTimeSlotAvailable(Long doctorId, LocalDateTime dateTime) {
        // // Business logic will be added in the specialized subject
        // TODO: Implement proper availability logic considering:
        // - Doctor's working hours
        // - Existing appointments
        // - Break times
        List<Appointment> conflicts = appointmentRepository.findConflictingAppointments(doctorId, dateTime);
        return conflicts.isEmpty();
    }

    /**
     * Validates that a patient exists via Patient Service.
     */
    private void validatePatientExists(Long patientId) {
        // // Business logic will be added in the specialized subject
        Boolean exists = patientClient.checkPatientExists(patientId);
        if (!Boolean.TRUE.equals(exists)) {
            throw new InvalidAppointmentException("Patient not found: " + patientId);
        }
    }

    /**
     * Validates that a doctor exists via Staff Service.
     */
    private void validateDoctorExists(Long doctorId) {
        // // Business logic will be added in the specialized subject
        Boolean exists = staffClient.checkStaffExists(doctorId);
        if (!Boolean.TRUE.equals(exists)) {
            throw new InvalidAppointmentException("Doctor not found: " + doctorId);
        }
    }
}

