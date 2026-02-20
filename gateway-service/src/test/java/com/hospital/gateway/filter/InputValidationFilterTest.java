package com.hospital.gateway.filter;

import com.hospital.gateway.audit.SecurityAuditSender;
import com.hospital.gateway.config.InputValidationProperties;
import com.hospital.gateway.validation.InjectionPatterns;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InputValidationFilter Unit Tests")
class InputValidationFilterTest {

    @Mock
    private GatewayFilterChain filterChain;

    @Mock
    private SecurityAuditSender securityAuditSender;

    private InputValidationProperties properties;
    private InjectionPatterns injectionPatterns;
    private InputValidationFilter filter;

    @BeforeEach
    void setUp() {
        properties = new InputValidationProperties();
        properties.setEnabled(true);
        properties.setExcludedPaths(List.of("/actuator/health"));
        injectionPatterns = new InjectionPatterns();
        filter = new InputValidationFilter(properties, injectionPatterns, securityAuditSender);
    }

    @Test
    @DisplayName("excluded path - chain called without validation")
    void filter_excludedPath_callsChain() {
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/actuator/health").build());
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, filterChain)).verifyComplete();

        verify(filterChain).filter(exchange);
        verify(securityAuditSender, never()).sendSuspiciousInput(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("suspicious query param - 400 and audit invoked with SQLI")
    void filter_suspiciousQueryParam_returns400_andAuditInvoked() {
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/patients/search").queryParam("query", "x OR 1=1").build());
        lenient().when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, filterChain)).verifyComplete();

        verify(filterChain, never()).filter(any());
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exchange.getResponse().getHeaders().getContentType().toString()).contains("application/json");

        ArgumentCaptor<String> eventTypeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> sourceCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> methodCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> categoryCaptor = ArgumentCaptor.forClass(String.class);
        verify(securityAuditSender).sendSuspiciousInput(
                eventTypeCaptor.capture(),
                sourceCaptor.capture(),
                pathCaptor.capture(),
                methodCaptor.capture(),
                categoryCaptor.capture());
        assertThat(eventTypeCaptor.getValue()).isEqualTo("SUSPICIOUS_INPUT");
        assertThat(sourceCaptor.getValue()).isEqualTo("query");
        assertThat(pathCaptor.getValue()).isEqualTo("/api/patients/search");
        assertThat(methodCaptor.getValue()).isEqualTo("GET");
        assertThat(categoryCaptor.getValue()).isEqualTo("SQLI");
    }

    @Test
    @DisplayName("suspicious header - 400 and audit invoked with XSS")
    void filter_suspiciousHeader_returns400_andAuditInvoked() {
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/patients/1")
                        .header("X-Custom-Header", "prefix <script>alert(1)</script>")
                        .build());
        lenient().when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, filterChain)).verifyComplete();

        verify(filterChain, never()).filter(any());
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        ArgumentCaptor<String> sourceCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> categoryCaptor = ArgumentCaptor.forClass(String.class);
        verify(securityAuditSender).sendSuspiciousInput(any(), sourceCaptor.capture(), any(), any(), categoryCaptor.capture());
        assertThat(sourceCaptor.getValue()).isEqualTo("header");
        assertThat(categoryCaptor.getValue()).isEqualTo("XSS");
    }

    @Test
    @DisplayName("safe query and headers - chain called")
    void filter_safeInput_callsChain() {
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/patients/search").queryParam("query", "Doe").build());
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, filterChain)).verifyComplete();

        verify(filterChain).filter(exchange);
        assertThat(exchange.getResponse().getStatusCode()).isNull();
        verify(securityAuditSender, never()).sendSuspiciousInput(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("validation disabled - chain called even with suspicious input")
    void filter_disabled_callsChain() {
        properties.setEnabled(false);
        filter = new InputValidationFilter(properties, injectionPatterns, securityAuditSender);
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/patients/search").queryParam("query", "OR 1=1").build());
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, filterChain)).verifyComplete();

        verify(filterChain).filter(exchange);
        verify(securityAuditSender, never()).sendSuspiciousInput(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("getOrder returns -80")
    void getOrder_returns80() {
        assertThat(filter.getOrder()).isEqualTo(-80);
    }
}
