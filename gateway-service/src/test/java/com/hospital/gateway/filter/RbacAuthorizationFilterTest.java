package com.hospital.gateway.filter;

import com.hospital.gateway.audit.SecurityAuditSender;
import com.hospital.gateway.rbac.Action;
import com.hospital.gateway.rbac.Resource;
import com.hospital.gateway.service.RbacService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RbacAuthorizationFilter Unit Tests")
class RbacAuthorizationFilterTest {

    @Mock
    private GatewayFilterChain filterChain;

    @Mock
    private RbacService rbacService;

    @Mock
    private SecurityAuditSender securityAuditSender;

    private RbacAuthorizationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new RbacAuthorizationFilter(rbacService, securityAuditSender);
    }

    @Test
    @DisplayName("path not in patient-dossier scope - chain is called")
    void filter_pathOutOfScope_callsChain() {
        ServerWebExchange exchange = exchange("/api/auth/login", "GET", "user1", "1", "ROLE_ADMIN");
        when(rbacService.resolveResource("/api/auth/login")).thenReturn(java.util.Optional.empty());
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, filterChain)).verifyComplete();

        verify(filterChain).filter(exchange);
        verify(securityAuditSender, never()).sendAccessDenied(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("patient path, allowed role (DOCTOR GET) - chain is called, no 403")
    void filter_allowedRole_callsChain() {
        ServerWebExchange exchange = exchange("/api/patients/1", "GET", "doctor1", "10", "ROLE_DOCTOR");
        when(rbacService.resolveResource("/api/patients/1")).thenReturn(java.util.Optional.of(Resource.PATIENTS));
        when(rbacService.isAllowed("/api/patients/1", "GET", List.of("ROLE_DOCTOR"), "10")).thenReturn(true);
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, filterChain)).verifyComplete();

        verify(filterChain).filter(exchange);
        assertThat(exchange.getResponse().getStatusCode()).isNull();
        verify(securityAuditSender, never()).sendAccessDenied(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("patient path, denied role (PATIENT GET /api/patients) - 403, chain not called, audit invoked")
    void filter_deniedRole_returns403_andAuditInvoked() {
        ServerWebExchange exchange = exchange("/api/patients", "GET", "patient1", "99", "ROLE_PATIENT");
        when(rbacService.resolveResource("/api/patients")).thenReturn(java.util.Optional.of(Resource.PATIENTS));
        when(rbacService.isAllowed("/api/patients", "GET", List.of("ROLE_PATIENT"), "99")).thenReturn(false);
        when(rbacService.resolveAction("GET")).thenReturn(Action.READ);
        when(rbacService.extractResourceId("/api/patients", Resource.PATIENTS)).thenReturn(null);

        StepVerifier.create(filter.filter(exchange, filterChain)).verifyComplete();

        verify(filterChain, never()).filter(any());
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(exchange.getResponse().getHeaders().getContentType().toString()).contains("application/json");

        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        // Response body is written via writeWith; we can read the committed response
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        ArgumentCaptor<String> userIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Resource> resourceCaptor = ArgumentCaptor.forClass(Resource.class);
        ArgumentCaptor<Action> actionCaptor = ArgumentCaptor.forClass(Action.class);
        verify(securityAuditSender).sendAccessDenied(
                userIdCaptor.capture(),
                resourceCaptor.capture(),
                any(),
                actionCaptor.capture(),
                any()
        );
        assertThat(userIdCaptor.getValue()).isEqualTo("99");
        assertThat(resourceCaptor.getValue()).isEqualTo(Resource.PATIENTS);
        assertThat(actionCaptor.getValue()).isEqualTo(Action.READ);
    }

    @Test
    @DisplayName("patient path, no X-Username - chain is called (RBAC skips unauthenticated)")
    void filter_noUsername_callsChain() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/patients/1").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        when(rbacService.resolveResource("/api/patients/1")).thenReturn(java.util.Optional.of(Resource.PATIENTS));
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, filterChain)).verifyComplete();

        verify(filterChain).filter(exchange);
        verify(rbacService, never()).isAllowed(any(), any(), any(), any());
    }

    @Test
    @DisplayName("DELETE /api/medical-records/1 with NURSE - 403 and audit with resourceId")
    void filter_medicalRecordsDelete_nurse_403_auditWithResourceId() {
        ServerWebExchange exchange = exchange("/api/medical-records/1", "DELETE", "nurse1", "20", "ROLE_NURSE");
        when(rbacService.resolveResource("/api/medical-records/1")).thenReturn(java.util.Optional.of(Resource.MEDICAL_RECORDS));
        when(rbacService.isAllowed("/api/medical-records/1", "DELETE", List.of("ROLE_NURSE"), "20")).thenReturn(false);
        when(rbacService.resolveAction("DELETE")).thenReturn(Action.DELETE);
        when(rbacService.extractResourceId("/api/medical-records/1", Resource.MEDICAL_RECORDS)).thenReturn("1");

        StepVerifier.create(filter.filter(exchange, filterChain)).verifyComplete();

        verify(filterChain, never()).filter(any());
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        ArgumentCaptor<String> resourceIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(securityAuditSender).sendAccessDenied(
                eq("20"),
                eq(Resource.MEDICAL_RECORDS),
                resourceIdCaptor.capture(),
                eq(Action.DELETE),
                eq("RBAC_DENY")
        );
        assertThat(resourceIdCaptor.getValue()).isEqualTo("1");
    }

    @Test
    @DisplayName("GET /api/patients/{id}/dossier with ROLE_DOCTOR - allowed, chain called (T6.4)")
    void filter_dossierGet_doctor_allowed() {
        ServerWebExchange exchange = exchange("/api/patients/1/dossier", "GET", "doctor1", "10", "ROLE_DOCTOR");
        when(rbacService.resolveResource("/api/patients/1/dossier")).thenReturn(java.util.Optional.of(Resource.PATIENTS));
        when(rbacService.isAllowed("/api/patients/1/dossier", "GET", List.of("ROLE_DOCTOR"), "10")).thenReturn(true);
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, filterChain)).verifyComplete();

        verify(filterChain).filter(exchange);
        assertThat(exchange.getResponse().getStatusCode()).isNull();
        verify(securityAuditSender, never()).sendAccessDenied(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("GET /api/patients/{id}/dossier with ROLE_PATIENT and wrong userId - 403 and audit (T6.4/T6.9)")
    void filter_dossierGet_patient_denied() {
        ServerWebExchange exchange = exchange("/api/patients/42/dossier", "GET", "patient1", "99", "ROLE_PATIENT");
        when(rbacService.resolveResource("/api/patients/42/dossier")).thenReturn(java.util.Optional.of(Resource.PATIENTS));
        when(rbacService.isAllowed("/api/patients/42/dossier", "GET", List.of("ROLE_PATIENT"), "99")).thenReturn(false);
        when(rbacService.resolveAction("GET")).thenReturn(Action.READ);
        when(rbacService.extractResourceId("/api/patients/42/dossier", Resource.PATIENTS)).thenReturn("42");

        StepVerifier.create(filter.filter(exchange, filterChain)).verifyComplete();

        verify(filterChain, never()).filter(any());
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        ArgumentCaptor<String> resourceIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(securityAuditSender).sendAccessDenied(
                eq("99"),
                eq(Resource.PATIENTS),
                resourceIdCaptor.capture(),
                eq(Action.READ),
                eq("RBAC_DENY")
        );
        assertThat(resourceIdCaptor.getValue()).isEqualTo("42");
    }

    @Test
    @DisplayName("GET /api/patients/{id}/dossier with ROLE_PATIENT and matching userId - allowed (T6.9)")
    void filter_dossierGet_patient_ownDossier_allowed() {
        ServerWebExchange exchange = exchange("/api/patients/42/dossier", "GET", "patient1", "42", "ROLE_PATIENT");
        when(rbacService.resolveResource("/api/patients/42/dossier")).thenReturn(java.util.Optional.of(Resource.PATIENTS));
        when(rbacService.isAllowed("/api/patients/42/dossier", "GET", List.of("ROLE_PATIENT"), "42")).thenReturn(true);
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, filterChain)).verifyComplete();

        verify(filterChain).filter(exchange);
        assertThat(exchange.getResponse().getStatusCode()).isNull();
        verify(securityAuditSender, never()).sendAccessDenied(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("DELETE /api/patients/{id} with ROLE_PATIENT and matching userId - allowed and emits PATIENT_SELF_DELETION_REQUESTED (T6.10, T6.11)")
    void filter_patientDelete_patient_ownRecord_allowed() {
        ServerWebExchange exchange = exchange("/api/patients/42", "DELETE", "patient1", "42", "ROLE_PATIENT");
        when(rbacService.resolveResource("/api/patients/42")).thenReturn(java.util.Optional.of(Resource.PATIENTS));
        when(rbacService.isAllowed("/api/patients/42", "DELETE", List.of("ROLE_PATIENT"), "42")).thenReturn(true);
        when(rbacService.resolveAction("DELETE")).thenReturn(Action.DELETE);
        when(rbacService.extractResourceId("/api/patients/42", Resource.PATIENTS)).thenReturn("42");
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, filterChain)).verifyComplete();

        verify(filterChain).filter(exchange);
        assertThat(exchange.getResponse().getStatusCode()).isNull();
        verify(securityAuditSender, never()).sendAccessDenied(any(), any(), any(), any(), any());
        verify(securityAuditSender).sendPatientSelfDeletionRequested("42");
    }

    @Test
    @DisplayName("DELETE /api/patients/{id} with ROLE_ADMIN - allowed, no PATIENT_SELF_DELETION_REQUESTED (T6.11)")
    void filter_patientDelete_admin_allowed_noSelfDeletionEvent() {
        ServerWebExchange exchange = exchange("/api/patients/42", "DELETE", "admin1", "1", "ROLE_ADMIN");
        when(rbacService.resolveResource("/api/patients/42")).thenReturn(java.util.Optional.of(Resource.PATIENTS));
        when(rbacService.isAllowed("/api/patients/42", "DELETE", List.of("ROLE_ADMIN"), "1")).thenReturn(true);
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, filterChain)).verifyComplete();

        verify(filterChain).filter(exchange);
        verify(securityAuditSender, never()).sendPatientSelfDeletionRequested(any());
    }

    @Test
    @DisplayName("DELETE /api/patients/{id} with ROLE_PATIENT and non-matching userId - 403 and audit (T6.10)")
    void filter_patientDelete_patient_otherRecord_denied() {
        ServerWebExchange exchange = exchange("/api/patients/42", "DELETE", "patient1", "99", "ROLE_PATIENT");
        when(rbacService.resolveResource("/api/patients/42")).thenReturn(java.util.Optional.of(Resource.PATIENTS));
        when(rbacService.isAllowed("/api/patients/42", "DELETE", List.of("ROLE_PATIENT"), "99")).thenReturn(false);
        when(rbacService.resolveAction("DELETE")).thenReturn(Action.DELETE);
        when(rbacService.extractResourceId("/api/patients/42", Resource.PATIENTS)).thenReturn("42");

        StepVerifier.create(filter.filter(exchange, filterChain)).verifyComplete();

        verify(filterChain, never()).filter(any());
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(securityAuditSender).sendAccessDenied(
                eq("99"),
                eq(Resource.PATIENTS),
                eq("42"),
                eq(Action.DELETE),
                eq("RBAC_DENY")
        );
    }

    @Test
    @DisplayName("getOrder returns -50")
    void getOrder_returns50() {
        assertThat(filter.getOrder()).isEqualTo(-50);
    }

    private static ServerWebExchange exchange(String path, String method, String username, String userId, String roles) {
        MockServerHttpRequest request = MockServerHttpRequest.method(org.springframework.http.HttpMethod.valueOf(method), path)
                .header("X-Username", username)
                .header("X-User-Id", userId)
                .header("X-User-Roles", roles)
                .build();
        return MockServerWebExchange.from(request);
    }
}
