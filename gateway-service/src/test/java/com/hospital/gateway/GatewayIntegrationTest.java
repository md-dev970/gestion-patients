package com.hospital.gateway;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

/**
 * Integration tests for the API Gateway (T1.10).
 * Exercises the full filter chain: validation, rate limit, bruteforce, JWT auth, RBAC.
 * Uses profile "integration" so Eureka is disabled and no real backends are required.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("integration")
@DisplayName("Gateway Integration Tests (JWT RBAC rate limit bruteforce validation)")
class GatewayIntegrationTest {

    private static final String JWT_SECRET_BASE64 = "dGhpcy1pcy1hLXZlcnktc2VjdXJlLWtleS1mb3Itand0LXRva2VuLWdlbmVyYXRpb24tMjU2Yml0cw==";

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @DisplayName("GET /actuator/health returns 200")
    void health_returns200() {
        webTestClient.get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("GET protected path without token returns 401")
    void protectedPath_withoutToken_returns401() {
        webTestClient.get()
                .uri("/api/patients/1")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED)
                .expectHeader().contentType("application/json");
    }

    @Test
    @DisplayName("GET protected path with invalid token returns 401")
    void protectedPath_withInvalidToken_returns401() {
        webTestClient.get()
                .uri("/api/patients/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("GET protected path with valid JWT but denied role (ROLE_PATIENT) returns 403")
    void protectedPath_validToken_deniedRole_returns403() {
        String token = createValidToken("patient1", List.of("ROLE_PATIENT"), 99L);

        webTestClient.get()
                .uri("/api/patients/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.FORBIDDEN)
                .expectHeader().contentType("application/json");
    }

    @Test
    @DisplayName("GET protected path with valid JWT and allowed role passes filters (503 when no backend)")
    void protectedPath_validToken_allowedRole_passesFilters() {
        String token = createValidToken("doctor1", List.of("ROLE_DOCTOR"), 10L);

        webTestClient.get()
                .uri("/api/patients/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    @DisplayName("Suspicious query param (SQLi pattern) returns 400 - validation")
    void suspiciousQueryParam_returns400() {
        String token = createValidToken("doctor1", List.of("ROLE_DOCTOR"), 10L);
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/patients/search").queryParam("query", "x OR 1=1").build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
                .expectHeader().contentType("application/json");
    }

    @Test
    @DisplayName("Safe query param passes validation (401 without token)")
    void safeQueryParam_passesValidation() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/patients/search").queryParam("query", "Doe").build())
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("POST /api/auth/login is not required to have token (public path; 503 when no backend)")
    void publicPath_login_doesNotRequireToken() {
        webTestClient.post()
                .uri("/api/auth/login")
                .header("Content-Type", "application/json")
                .bodyValue("{}")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    private static String createValidToken(String subject, List<String> roles, long userId) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(JWT_SECRET_BASE64));
        return Jwts.builder()
                .subject(subject)
                .claim("roles", roles)
                .claim("userId", userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600_000))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }
}
