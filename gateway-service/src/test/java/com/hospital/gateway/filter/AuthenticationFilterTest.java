package com.hospital.gateway.filter;

import com.hospital.gateway.service.JwtClaims;
import com.hospital.gateway.service.JwtVerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationFilter Unit Tests")
class AuthenticationFilterTest {

    @Mock
    private GatewayFilterChain filterChain;

    @Mock
    private JwtVerificationService jwtVerificationService;

    private AuthenticationFilter authenticationFilter;

    @BeforeEach
    void setUp() {
        authenticationFilter = new AuthenticationFilter(jwtVerificationService);
    }

    @Test
    @DisplayName("filter - public path /api/auth/login - allows request without token")
    void filter_publicPathLogin_allowsRequest() {
        ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/auth/login").build());
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        StepVerifier.create(authenticationFilter.filter(exchange, filterChain))
                .verifyComplete();
        verify(filterChain).filter(exchange);
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    @DisplayName("filter - public path /api/auth/register - allows request")
    void filter_publicPathRegister_allowsRequest() {
        ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/auth/register").build());
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        StepVerifier.create(authenticationFilter.filter(exchange, filterChain))
                .verifyComplete();
        verify(filterChain).filter(exchange);
    }

    @Test
    @DisplayName("filter - public path /actuator/health - allows request")
    void filter_publicPathActuatorHealth_allowsRequest() {
        ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/actuator/health").build());
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        StepVerifier.create(authenticationFilter.filter(exchange, filterChain))
                .verifyComplete();
        verify(filterChain).filter(exchange);
    }

    @Test
    @DisplayName("filter - private path without token - returns 401 and does not call chain")
    void filter_privatePathWithoutToken_returns401() {
        ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/patients/1").build());

        StepVerifier.create(authenticationFilter.filter(exchange, filterChain))
                .verifyComplete();

        verify(filterChain, never()).filter(any());
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exchange.getResponse().getHeaders().getContentType().toString()).contains("application/json");
    }

    @Test
    @DisplayName("filter - private path with invalid token - returns 401 and does not call chain")
    void filter_privatePathWithInvalidToken_returns401() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer invalid-token");
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/patients/1").headers(headers).build());

        when(jwtVerificationService.verifyAndGetClaims("invalid-token")).thenReturn(Optional.empty());

        StepVerifier.create(authenticationFilter.filter(exchange, filterChain))
                .verifyComplete();

        verify(filterChain, never()).filter(any());
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("filter - private path with valid token - calls chain with X-User-* headers")
    void filter_privatePathWithValidToken_callsChainWithHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer valid-token");
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/patients/1").headers(headers).build());

        JwtClaims claims = new JwtClaims("doctor1", List.of("DOCTOR"), 10L, 2L);
        when(jwtVerificationService.verifyAndGetClaims("valid-token")).thenReturn(Optional.of(claims));
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        StepVerifier.create(authenticationFilter.filter(exchange, filterChain))
                .verifyComplete();

        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(filterChain).filter(captor.capture());
        ServerWebExchange mutated = captor.getValue();
        assertThat(mutated.getRequest().getHeaders().getFirst("X-Username")).isEqualTo("doctor1");
        assertThat(mutated.getRequest().getHeaders().getFirst("X-User-Id")).isEqualTo("10");
        assertThat(mutated.getRequest().getHeaders().getFirst("X-User-Roles")).isEqualTo("DOCTOR");
    }

    @Test
    @DisplayName("filter - private path with Authorization not Bearer - returns 401")
    void filter_privatePathWithNonBearer_returns401() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic dXNlcjpwYXNz");
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/patients/1").headers(headers).build());

        StepVerifier.create(authenticationFilter.filter(exchange, filterChain))
                .verifyComplete();

        verify(filterChain, never()).filter(any());
        verify(jwtVerificationService, never()).verifyAndGetClaims(any());
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("getOrder - returns high priority value")
    void getOrder_returnsHighPriorityValue() {
        assertThat(authenticationFilter.getOrder()).isEqualTo(-100);
    }
}
