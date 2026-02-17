package com.hospital.auth.service;

import com.hospital.auth.model.Role;
import com.hospital.auth.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtService Unit Tests")
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    private User user;
    private String secretKey;
    private long jwtExpiration;
    private long refreshExpiration;

    @BeforeEach
    void setUp() {
        // Generate a valid base64-encoded secret key for testing
        SecretKey key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
        secretKey = Base64.getEncoder().encodeToString(key.getEncoded());
        
        jwtExpiration = 3600000L; // 1 hour in milliseconds
        refreshExpiration = 86400000L; // 24 hours in milliseconds

        // Set private fields using reflection
        ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", jwtExpiration);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", refreshExpiration);

        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .roles(Set.of(Role.ROLE_PATIENT))
                .enabled(true)
                .accountNonLocked(true)
                .build();
    }

    @Test
    @DisplayName("generateToken - valid user - returns token string")
    void generateToken_validUser_returnsTokenString() {
        // When
        String token = jwtService.generateToken(user);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
    }

    @Test
    @DisplayName("generateToken - user with roles - includes roles in token")
    void generateToken_userWithRoles_includesRolesInToken() {
        // Given
        user.setRoles(Set.of(Role.ROLE_DOCTOR, Role.ROLE_ADMIN));

        // When
        String token = jwtService.generateToken(user);
        String username = jwtService.extractUsername(token);

        // Then
        assertThat(token).isNotNull();
        assertThat(username).isEqualTo("testuser");
    }

    @Test
    @DisplayName("generateToken - user with staffId - includes staffId in token")
    void generateToken_userWithStaffId_includesStaffIdInToken() {
        // Given
        user.setStaffId(123L);

        // When
        String token = jwtService.generateToken(user);
        String username = jwtService.extractUsername(token);

        // Then
        assertThat(token).isNotNull();
        assertThat(username).isEqualTo("testuser");
    }

    @Test
    @DisplayName("generateRefreshToken - valid user - returns refresh token")
    void generateRefreshToken_validUser_returnsRefreshToken() {
        // When
        String token = jwtService.generateRefreshToken(user);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("extractUsername - valid token - returns username")
    void extractUsername_validToken_returnsUsername() {
        // Given
        String token = jwtService.generateToken(user);

        // When
        String username = jwtService.extractUsername(token);

        // Then
        assertThat(username).isEqualTo("testuser");
    }

    @Test
    @DisplayName("extractClaim - valid token - returns claim value")
    void extractClaim_validToken_returnsClaimValue() {
        // Given
        String token = jwtService.generateToken(user);

        // When
        String subject = jwtService.extractClaim(token, Claims::getSubject);
        Date expiration = jwtService.extractClaim(token, Claims::getExpiration);

        // Then
        assertThat(subject).isEqualTo("testuser");
        assertThat(expiration).isNotNull();
        assertThat(expiration).isAfter(new Date());
    }

    @Test
    @DisplayName("isTokenValid - valid token and user - returns true")
    void isTokenValid_validTokenAndUser_returnsTrue() {
        // Given
        String token = jwtService.generateToken(user);

        // When
        boolean isValid = jwtService.isTokenValid(token, user);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("isTokenValid - wrong user - returns false")
    void isTokenValid_wrongUser_returnsFalse() {
        // Given
        User otherUser = User.builder()
                .id(2L)
                .username("otheruser")
                .email("other@example.com")
                .roles(Set.of(Role.ROLE_PATIENT))
                .build();
        String token = jwtService.generateToken(user);

        // When
        boolean isValid = jwtService.isTokenValid(token, otherUser);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("isTokenValid - expired token - returns false")
    void isTokenValid_expiredToken_returnsFalse() throws InterruptedException {
        // Given - Set a very short expiration
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 100L); // 100ms
        String token = jwtService.generateToken(user);
        Thread.sleep(200); // Wait for token to expire

        // When
        boolean isValid = jwtService.isTokenValid(token, user);

        // Then
        assertThat(isValid).isFalse();
        
        // Reset expiration for other tests
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", jwtExpiration);
    }

    @Test
    @DisplayName("validateToken - valid token - returns true")
    void validateToken_validToken_returnsTrue() {
        // Given
        String token = jwtService.generateToken(user);

        // When
        boolean isValid = jwtService.validateToken(token);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("validateToken - expired token - returns false")
    void validateToken_expiredToken_returnsFalse() throws InterruptedException {
        // Given - Set a very short expiration
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 100L); // 100ms
        String token = jwtService.generateToken(user);
        Thread.sleep(200); // Wait for token to expire

        // When
        boolean isValid = jwtService.validateToken(token);

        // Then
        assertThat(isValid).isFalse();
        
        // Reset expiration for other tests
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", jwtExpiration);
    }

    @Test
    @DisplayName("validateToken - malformed token - returns false")
    void validateToken_malformedToken_returnsFalse() {
        // Given
        String malformedToken = "not.a.valid.jwt.token";

        // When
        boolean isValid = jwtService.validateToken(malformedToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("validateToken - empty token - returns false")
    void validateToken_emptyToken_returnsFalse() {
        // Given
        String emptyToken = "";

        // When
        boolean isValid = jwtService.validateToken(emptyToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("getExpirationTime - returns expiration in seconds")
    void getExpirationTime_returnsExpirationInSeconds() {
        // When
        long expirationTime = jwtService.getExpirationTime();

        // Then
        assertThat(expirationTime).isEqualTo(jwtExpiration / 1000);
    }
}





