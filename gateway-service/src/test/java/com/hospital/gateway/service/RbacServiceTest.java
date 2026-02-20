package com.hospital.gateway.service;

import com.hospital.gateway.rbac.Action;
import com.hospital.gateway.rbac.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RbacService Unit Tests")
class RbacServiceTest {

    private RbacService rbacService;

    @BeforeEach
    void setUp() {
        rbacService = new RbacService();
    }

    @Test
    @DisplayName("path outside patient resources is allowed")
    void isAllowed_pathOutsideScope_returnsTrue() {
        assertThat(rbacService.isAllowed("/api/auth/login", "POST", List.of())).isTrue();
        assertThat(rbacService.isAllowed("/api/staff/1", "GET", List.of())).isTrue();
        assertThat(rbacService.isAllowed("/api/appointments", "GET", List.of())).isTrue();
    }

    @Test
    @DisplayName("GET /api/patients with ROLE_DOCTOR is allowed")
    void isAllowed_patientsRead_doctor_allowed() {
        assertThat(rbacService.isAllowed("/api/patients", "GET", List.of("ROLE_DOCTOR"))).isTrue();
        assertThat(rbacService.isAllowed("/api/patients/1", "GET", List.of("ROLE_MEDECIN"))).isTrue();
    }

    @Test
    @DisplayName("GET /api/patients with ROLE_PATIENT is denied")
    void isAllowed_patientsRead_patient_denied() {
        assertThat(rbacService.isAllowed("/api/patients", "GET", List.of("ROLE_PATIENT"))).isFalse();
        assertThat(rbacService.isAllowed("/api/patients/1", "GET", List.of("ROLE_PATIENT"))).isFalse();
    }

    @Test
    @DisplayName("DELETE /api/patients with ROLE_NURSE is denied")
    void isAllowed_patientsDelete_nurse_denied() {
        assertThat(rbacService.isAllowed("/api/patients/1", "DELETE", List.of("ROLE_NURSE"))).isFalse();
    }

    @Test
    @DisplayName("DELETE /api/patients with ROLE_ADMIN is allowed")
    void isAllowed_patientsDelete_admin_allowed() {
        assertThat(rbacService.isAllowed("/api/patients/1", "DELETE", List.of("ROLE_ADMIN"))).isTrue();
    }

    @Test
    @DisplayName("GET /api/medical-records with ROLE_LAB_TECH is allowed")
    void isAllowed_medicalRecordsRead_labTech_allowed() {
        assertThat(rbacService.isAllowed("/api/medical-records/patient/1", "GET", List.of("ROLE_LAB_TECH"))).isTrue();
    }

    @Test
    @DisplayName("DELETE /api/medical-records with ROLE_NURSE is denied")
    void isAllowed_medicalRecordsDelete_nurse_denied() {
        assertThat(rbacService.isAllowed("/api/medical-records/1", "DELETE", List.of("ROLE_NURSE"))).isFalse();
    }

    @Test
    @DisplayName("GET /api/consultations with ROLE_RECEPTIONIST - RECEPTIONIST has no consultations READ")
    void isAllowed_consultationsRead_receptionist_denied() {
        assertThat(rbacService.isAllowed("/api/consultations", "GET", List.of("ROLE_RECEPTIONIST"))).isFalse();
    }

    @Test
    @DisplayName("empty roles on patient path is denied")
    void isAllowed_patientPath_emptyRoles_denied() {
        assertThat(rbacService.isAllowed("/api/patients/1", "GET", List.of())).isFalse();
        assertThat(rbacService.isAllowed("/api/patients", "GET", null)).isFalse();
    }

    @Test
    @DisplayName("resolveResource returns PATIENTS for /api/patients path")
    void resolveResource_patientsPrefix_returnsPATIENTS() {
        assertThat(rbacService.resolveResource("/api/patients")).contains(Resource.PATIENTS);
        assertThat(rbacService.resolveResource("/api/patients/123")).contains(Resource.PATIENTS);
    }

    @Test
    @DisplayName("resolveResource returns empty for non-patient-dossier path")
    void resolveResource_otherPath_returnsEmpty() {
        assertThat(rbacService.resolveResource("/api/auth/login")).isEmpty();
        assertThat(rbacService.resolveResource(null)).isEmpty();
    }

    @Test
    @DisplayName("resolveAction maps HTTP method to action")
    void resolveAction_mapsCorrectly() {
        assertThat(rbacService.resolveAction("GET")).isEqualTo(Action.READ);
        assertThat(rbacService.resolveAction("POST")).isEqualTo(Action.CREATE);
        assertThat(rbacService.resolveAction("PUT")).isEqualTo(Action.UPDATE);
        assertThat(rbacService.resolveAction("PATCH")).isEqualTo(Action.UPDATE);
        assertThat(rbacService.resolveAction("DELETE")).isEqualTo(Action.DELETE);
    }

    @Test
    @DisplayName("extractResourceId extracts numeric id from path")
    void extractResourceId_extractsId() {
        assertThat(rbacService.extractResourceId("/api/patients/123", Resource.PATIENTS)).isEqualTo("123");
        assertThat(rbacService.extractResourceId("/api/medical-records/456", Resource.MEDICAL_RECORDS)).isEqualTo("456");
        assertThat(rbacService.extractResourceId("/api/patients", Resource.PATIENTS)).isNull();
        assertThat(rbacService.extractResourceId("/api/patients/search", Resource.PATIENTS)).isNull();
    }
}
