package com.hospital.patient.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.patient.dto.AppointmentSummaryDTO;
import com.hospital.patient.dto.ConsultationSummaryDTO;
import com.hospital.patient.dto.MedicalRecordSummaryDTO;
import com.hospital.patient.dto.PatientDTO;
import com.hospital.patient.dto.PatientDossierDTO;
import com.hospital.patient.exception.PatientNotFoundException;
import com.hospital.patient.service.PatientService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PatientController.class)
@DisplayName("PatientController GDPR endpoints (T6.5)")
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PatientService patientService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/patients/{id}/dossier - patient found - returns aggregated dossier")
    void getPatientDossier_patientFound_returnsAggregatedDossier() throws Exception {
        PatientDTO patient = PatientDTO.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .nationalId("AB123456")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .build();

        MedicalRecordSummaryDTO record = MedicalRecordSummaryDTO.builder()
                .id(10L)
                .patientId(1L)
                .allergies("Peanuts")
                .build();

        ConsultationSummaryDTO consultation = ConsultationSummaryDTO.builder()
                .consultationId(UUID.randomUUID())
                .patientId(1L)
                .diagnostic("Flu")
                .build();

        AppointmentSummaryDTO appointment = AppointmentSummaryDTO.builder()
                .id(100L)
                .patientId(1L)
                .reason("Checkup")
                .build();

        PatientDossierDTO dossier = PatientDossierDTO.builder()
                .patient(patient)
                .medicalRecord(record)
                .consultations(List.of(consultation))
                .appointments(List.of(appointment))
                .build();

        when(patientService.getPatientDossier(1L)).thenReturn(dossier);

        mockMvc.perform(get("/api/patients/1/dossier")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patient.id").value(1))
                .andExpect(jsonPath("$.medicalRecord.patientId").value(1))
                .andExpect(jsonPath("$.consultations[0].patientId").value(1))
                .andExpect(jsonPath("$.appointments[0].patientId").value(1));
    }

    @Test
    @DisplayName("GET /api/patients/{id}/dossier - patient not found - returns 404")
    void getPatientDossier_patientNotFound_returns404() throws Exception {
        when(patientService.getPatientDossier(anyLong()))
                .thenThrow(new PatientNotFoundException("Patient not found with ID: 1"));

        mockMvc.perform(get("/api/patients/1/dossier")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/patients/{id}/dossier/export - patient found - returns attachment with dossier JSON")
    void exportPatientDossier_patientFound_returnsAttachment() throws Exception {
        PatientDTO patient = PatientDTO.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .build();

        MedicalRecordSummaryDTO record = MedicalRecordSummaryDTO.builder()
                .id(10L)
                .patientId(1L)
                .build();

        PatientDossierDTO dossier = PatientDossierDTO.builder()
                .patient(patient)
                .medicalRecord(record)
                .consultations(List.of())
                .appointments(List.of())
                .build();

        when(patientService.getPatientDossier(1L)).thenReturn(dossier);

        mockMvc.perform(get("/api/patients/1/dossier/export")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"patient-1-dossier.json\""))
                .andExpect(jsonPath("$.patient.id").value(1));
    }

    @Test
    @DisplayName("GET /api/patients/{id}/dossier/export - returns JSON content type (T6.7)")
    void exportPatientDossier_returnsJsonContentType() throws Exception {
        PatientDossierDTO dossier = PatientDossierDTO.builder()
                .patient(PatientDTO.builder().id(1L).firstName("J").lastName("D").build())
                .medicalRecord(null)
                .consultations(List.of())
                .appointments(List.of())
                .build();
        when(patientService.getPatientDossier(1L)).thenReturn(dossier);

        mockMvc.perform(get("/api/patients/1/dossier/export")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("GET /api/patients/{id}/dossier/export - patient not found - returns 404")
    void exportPatientDossier_patientNotFound_returns404() throws Exception {
        when(patientService.getPatientDossier(anyLong()))
                .thenThrow(new PatientNotFoundException("Patient not found with ID: 1"));

        mockMvc.perform(get("/api/patients/1/dossier/export")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/patients/{id} - patient found - returns 204")
    void deletePatient_patientFound_returns204() throws Exception {
        // No exception from service -> 204
        mockMvc.perform(delete("/api/patients/1"))
                .andExpect(status().isNoContent());

        Mockito.verify(patientService).deletePatient(1L);
    }

    @Test
    @DisplayName("DELETE /api/patients/{id} - patient not found - returns 404")
    void deletePatient_patientNotFound_returns404() throws Exception {
        doThrow(new PatientNotFoundException("Patient not found with ID: 1"))
                .when(patientService).deletePatient(1L);

        mockMvc.perform(delete("/api/patients/1"))
                .andExpect(status().isNotFound());
    }
}

