package com.hospital.patient.service.impl;

import com.hospital.patient.client.AppointmentClient;
import com.hospital.patient.client.ConsultationClient;
import com.hospital.patient.client.MedicalRecordClient;
import com.hospital.patient.dto.AppointmentSummaryDTO;
import com.hospital.patient.dto.ConsultationSummaryDTO;
import com.hospital.patient.dto.MedicalRecordSummaryDTO;
import com.hospital.patient.dto.PatientCreateRequest;
import com.hospital.patient.dto.PatientDTO;
import com.hospital.patient.dto.PatientDossierDTO;
import com.hospital.patient.exception.DuplicatePatientException;
import com.hospital.patient.exception.PatientNotFoundException;
import com.hospital.patient.mapper.PatientMapper;
import com.hospital.patient.model.Patient;
import com.hospital.patient.audit.SecurityAuditSender;
import com.hospital.patient.config.RetentionProperties;
import com.hospital.patient.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("PatientServiceImpl Unit Tests")
class PatientServiceImplTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PatientMapper patientMapper;

    @Mock
    private MedicalRecordClient medicalRecordClient;

    @Mock
    private ConsultationClient consultationClient;

    @Mock
    private AppointmentClient appointmentClient;

    @Mock
    private SecurityAuditSender securityAuditSender;

    @Mock
    private RetentionProperties retentionProperties;

    @InjectMocks
    private PatientServiceImpl patientService;

    private PatientCreateRequest createRequest;
    private Patient patient;
    private PatientDTO patientDTO;

    @BeforeEach
    void setUp() {
        lenient().when(retentionProperties.getPatientYears()).thenReturn(10);
        createRequest = PatientCreateRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .nationalId("AB123456")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .email("john.doe@example.com")
                .phoneNumber("1234567890")
                .address("123 Main St")
                .build();

        patient = Patient.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .nationalId("AB123456")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .email("john.doe@example.com")
                .phoneNumber("1234567890")
                .address("123 Main St")
                .build();

        patientDTO = PatientDTO.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .nationalId("AB123456")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .email("john.doe@example.com")
                .phoneNumber("1234567890")
                .address("123 Main St")
                .build();
    }

    @Test
    @DisplayName("createPatient - valid request - returns PatientDTO")
    void createPatient_validRequest_returnsPatientDTO() {
        // Given
        when(patientRepository.existsByNationalId("AB123456")).thenReturn(false);
        when(patientMapper.toEntity(createRequest)).thenReturn(patient);
        when(patientRepository.save(patient)).thenReturn(patient);
        when(patientMapper.toDTO(patient)).thenReturn(patientDTO);

        // When
        PatientDTO result = patientService.createPatient(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNationalId()).isEqualTo("AB123456");
        verify(patientRepository).existsByNationalId("AB123456");
        verify(patientMapper).toEntity(createRequest);
        verify(patientRepository).save(patient);
        verify(patientMapper).toDTO(patient);
        verify(securityAuditSender).sendPhiAccessed("PATIENT", "1", "CREATE");
    }

    @Test
    @DisplayName("createPatient - sets retentionUntil from config")
    void createPatient_setsRetentionUntil() {
        when(patientRepository.existsByNationalId("AB123456")).thenReturn(false);
        when(patientMapper.toEntity(createRequest)).thenReturn(patient);
        when(patientRepository.save(any(Patient.class))).thenAnswer(inv -> inv.getArgument(0));
        when(patientMapper.toDTO(any(Patient.class))).thenReturn(patientDTO);
        when(retentionProperties.getPatientYears()).thenReturn(10);

        patientService.createPatient(createRequest);

        verify(patientRepository).save(argThat(p -> p.getRetentionUntil() != null
                && p.getRetentionUntil().equals(LocalDate.now().plusYears(10))));
    }

    @Test
    @DisplayName("createPatient - duplicate national ID - throws DuplicatePatientException")
    void createPatient_duplicateNationalId_throwsDuplicatePatientException() {
        // Given
        when(patientRepository.existsByNationalId("AB123456")).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> patientService.createPatient(createRequest))
                .isInstanceOf(DuplicatePatientException.class)
                .hasMessageContaining("already exists");
        verify(patientRepository).existsByNationalId("AB123456");
        verify(patientRepository, never()).save(any());
    }

    @Test
    @DisplayName("getPatientById - patient found - returns Optional with PatientDTO")
    void getPatientById_patientFound_returnsOptionalWithPatientDTO() {
        // Given
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(patientMapper.toDTO(patient)).thenReturn(patientDTO);

        // When
        Optional<PatientDTO> result = patientService.getPatientById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        verify(patientRepository).findById(1L);
        verify(patientMapper).toDTO(patient);
        verify(securityAuditSender).sendPhiAccessed("PATIENT", "1", "READ");
    }

    @Test
    @DisplayName("getPatientById - patient not found - returns empty Optional")
    void getPatientById_patientNotFound_returnsEmptyOptional() {
        // Given
        when(patientRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        Optional<PatientDTO> result = patientService.getPatientById(1L);

        // Then
        assertThat(result).isEmpty();
        verify(patientRepository).findById(1L);
        verify(patientMapper, never()).toDTO(any());
    }

    @Test
    @DisplayName("getPatientByNationalId - patient found - returns Optional with PatientDTO")
    void getPatientByNationalId_patientFound_returnsOptionalWithPatientDTO() {
        // Given
        when(patientRepository.findByNationalId("AB123456")).thenReturn(Optional.of(patient));
        when(patientMapper.toDTO(patient)).thenReturn(patientDTO);

        // When
        Optional<PatientDTO> result = patientService.getPatientByNationalId("AB123456");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getNationalId()).isEqualTo("AB123456");
        verify(patientRepository).findByNationalId("AB123456");
        verify(patientMapper).toDTO(patient);
    }

    @Test
    @DisplayName("getPatientByNationalId - patient not found - returns empty Optional")
    void getPatientByNationalId_patientNotFound_returnsEmptyOptional() {
        // Given
        when(patientRepository.findByNationalId("AB123456")).thenReturn(Optional.empty());

        // When
        Optional<PatientDTO> result = patientService.getPatientByNationalId("AB123456");

        // Then
        assertThat(result).isEmpty();
        verify(patientRepository).findByNationalId("AB123456");
        verify(patientMapper, never()).toDTO(any());
    }

    @Test
    @DisplayName("getAllPatients - with results - returns list of PatientDTO")
    void getAllPatients_withResults_returnsListOfPatientDTO() {
        // Given
        List<Patient> patients = List.of(patient);
        when(patientRepository.findAll()).thenReturn(patients);
        when(patientMapper.toDTO(patient)).thenReturn(patientDTO);

        // When
        List<PatientDTO> result = patientService.getAllPatients();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        verify(patientRepository).findAll();
    }

    @Test
    @DisplayName("getAllPatients - empty results - returns empty list")
    void getAllPatients_emptyResults_returnsEmptyList() {
        // Given
        when(patientRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<PatientDTO> result = patientService.getAllPatients();

        // Then
        assertThat(result).isEmpty();
        verify(patientRepository).findAll();
    }

    @Test
    @DisplayName("searchPatients - matching results - returns filtered list")
    void searchPatients_matchingResults_returnsFilteredList() {
        // Given
        String searchTerm = "Doe";
        List<Patient> patients = List.of(patient);
        when(patientRepository.findByLastNameContainingIgnoreCase(searchTerm)).thenReturn(patients);
        when(patientMapper.toDTO(patient)).thenReturn(patientDTO);

        // When
        List<PatientDTO> result = patientService.searchPatients(searchTerm);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLastName()).contains("Doe");
        verify(patientRepository).findByLastNameContainingIgnoreCase(searchTerm);
    }

    @Test
    @DisplayName("searchPatients - no matches - returns empty list")
    void searchPatients_noMatches_returnsEmptyList() {
        // Given
        String searchTerm = "Smith";
        when(patientRepository.findByLastNameContainingIgnoreCase(searchTerm))
                .thenReturn(Collections.emptyList());

        // When
        List<PatientDTO> result = patientService.searchPatients(searchTerm);

        // Then
        assertThat(result).isEmpty();
        verify(patientRepository).findByLastNameContainingIgnoreCase(searchTerm);
    }

    @Test
    @DisplayName("updatePatient - patient found - returns updated PatientDTO")
    void updatePatient_patientFound_returnsUpdatedPatientDTO() {
        // Given
        PatientDTO updateDTO = PatientDTO.builder()
                .id(1L)
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.com")
                .build();

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(patientRepository.save(patient)).thenReturn(patient);
        when(patientMapper.toDTO(patient)).thenReturn(updateDTO);

        // When
        PatientDTO result = patientService.updatePatient(1L, updateDTO);

        // Then
        assertThat(result).isNotNull();
        verify(patientRepository).findById(1L);
        verify(patientMapper).updateEntityFromDTO(updateDTO, patient);
        verify(patientRepository).save(patient);
        verify(patientMapper).toDTO(patient);
        verify(securityAuditSender).sendPhiAccessed("PATIENT", "1", "UPDATE");
    }

    @Test
    @DisplayName("updatePatient - patient not found - throws PatientNotFoundException")
    void updatePatient_patientNotFound_throwsPatientNotFoundException() {
        // Given
        PatientDTO updateDTO = PatientDTO.builder().id(1L).build();
        when(patientRepository.findById(1L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> patientService.updatePatient(1L, updateDTO))
                .isInstanceOf(PatientNotFoundException.class)
                .hasMessageContaining("Patient not found");
        verify(patientRepository).findById(1L);
        verify(patientRepository, never()).save(any());
    }

    @Test
    @DisplayName("deletePatient - patient found - deletes patient and sends PHI_DELETED")
    void deletePatient_patientFound_deletesPatientAndSendsPhiDeleted() {
        // Given
        when(patientRepository.existsById(1L)).thenReturn(true);
        doNothing().when(medicalRecordClient).deleteMedicalRecordsByPatientId(1L);
        doNothing().when(consultationClient).deleteConsultationsByPatientId(1L);
        doNothing().when(appointmentClient).deleteAppointmentsByPatientId(1L);
        doNothing().when(patientRepository).deleteById(1L);

        // When
        patientService.deletePatient(1L);

        // Then
        verify(patientRepository).existsById(1L);
        verify(medicalRecordClient).deleteMedicalRecordsByPatientId(1L);
        verify(consultationClient).deleteConsultationsByPatientId(1L);
        verify(appointmentClient).deleteAppointmentsByPatientId(1L);
        verify(patientRepository).deleteById(1L);
        verify(securityAuditSender).sendPhiDeleted("PATIENT", "1");
    }

    @Test
    @DisplayName("deletePatient - patient not found - throws PatientNotFoundException, no PHI_DELETED")
    void deletePatient_patientNotFound_throwsPatientNotFoundException() {
        // Given
        when(patientRepository.existsById(1L)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> patientService.deletePatient(1L))
                .isInstanceOf(PatientNotFoundException.class)
                .hasMessageContaining("Patient not found");
        verify(patientRepository).existsById(1L);
        verify(medicalRecordClient, never()).deleteMedicalRecordsByPatientId(anyLong());
        verify(consultationClient, never()).deleteConsultationsByPatientId(anyLong());
        verify(appointmentClient, never()).deleteAppointmentsByPatientId(anyLong());
        verify(patientRepository, never()).deleteById(any());
        verify(securityAuditSender, never()).sendPhiDeleted(any(), any());
    }

    @Test
    @DisplayName("getPatientDossier - patient found - aggregates data from downstream services")
    void getPatientDossier_patientFound_aggregatesData() {
        // Given
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(patientMapper.toDTO(patient)).thenReturn(patientDTO);

        MedicalRecordSummaryDTO record = MedicalRecordSummaryDTO.builder()
                .id(10L)
                .patientId(1L)
                .allergies("Peanuts")
                .build();
        when(medicalRecordClient.getMedicalRecordByPatientId(1L)).thenReturn(record);

        ConsultationSummaryDTO consultation = ConsultationSummaryDTO.builder()
                .consultationId(java.util.UUID.randomUUID())
                .patientId(1L)
                .diagnostic("Flu")
                .build();
        when(consultationClient.getConsultationsByPatientId(1L))
                .thenReturn(java.util.List.of(consultation));

        AppointmentSummaryDTO appointment = AppointmentSummaryDTO.builder()
                .id(100L)
                .patientId(1L)
                .reason("Checkup")
                .build();
        when(appointmentClient.getAppointmentsByPatientId(1L))
                .thenReturn(java.util.List.of(appointment));

        // When
        PatientDossierDTO dossier = patientService.getPatientDossier(1L);

        // Then
        assertThat(dossier).isNotNull();
        assertThat(dossier.getPatient()).isNotNull();
        assertThat(dossier.getMedicalRecord()).isEqualTo(record);
        assertThat(dossier.getConsultations()).hasSize(1);
        assertThat(dossier.getAppointments()).hasSize(1);

        verify(medicalRecordClient).getMedicalRecordByPatientId(1L);
        verify(consultationClient).getConsultationsByPatientId(1L);
        verify(appointmentClient).getAppointmentsByPatientId(1L);
    }

    @Test
    @DisplayName("getPatientDossier - patient not found - throws PatientNotFoundException")
    void getPatientDossier_patientNotFound_throwsPatientNotFoundException() {
        // Given
        when(patientRepository.findById(1L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> patientService.getPatientDossier(1L))
                .isInstanceOf(PatientNotFoundException.class)
                .hasMessageContaining("Patient not found");

        verify(medicalRecordClient, never()).getMedicalRecordByPatientId(anyLong());
        verify(consultationClient, never()).getConsultationsByPatientId(anyLong());
        verify(appointmentClient, never()).getAppointmentsByPatientId(anyLong());
    }

    @Test
    @DisplayName("existsById - patient exists - returns true")
    void existsById_patientExists_returnsTrue() {
        // Given
        when(patientRepository.existsById(1L)).thenReturn(true);

        // When
        boolean result = patientService.existsById(1L);

        // Then
        assertThat(result).isTrue();
        verify(patientRepository).existsById(1L);
    }

    @Test
    @DisplayName("existsById - patient does not exist - returns false")
    void existsById_patientDoesNotExist_returnsFalse() {
        // Given
        when(patientRepository.existsById(1L)).thenReturn(false);

        // When
        boolean result = patientService.existsById(1L);

        // Then
        assertThat(result).isFalse();
        verify(patientRepository).existsById(1L);
    }

    @Test
    @DisplayName("withdrawConsent - patient found - sets consentGiven false and sends PHI_ACCESS")
    void withdrawConsent_patientFound_updatesAndSendsPhiAccessed() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(patientRepository.save(any(Patient.class))).thenAnswer(inv -> inv.getArgument(0));
        when(patientMapper.toDTO(any(Patient.class))).thenReturn(patientDTO);

        PatientDTO result = patientService.withdrawConsent(1L);

        assertThat(result).isNotNull();
        verify(patientRepository).save(argThat(p -> !p.isConsentGiven() && "withdrawn".equals(p.getLegalBasis())));
        verify(securityAuditSender).sendPhiAccessed("PATIENT", "1", "UPDATE");
    }

    @Test
    @DisplayName("withdrawConsent - patient not found - throws PatientNotFoundException")
    void withdrawConsent_patientNotFound_throws() {
        when(patientRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.withdrawConsent(1L))
                .isInstanceOf(PatientNotFoundException.class);
        verify(securityAuditSender, never()).sendPhiAccessed(any(), any(), any());
    }
}


