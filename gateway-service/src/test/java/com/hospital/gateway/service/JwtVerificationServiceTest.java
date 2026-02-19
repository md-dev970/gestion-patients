package com.hospital.gateway.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = { JwtVerificationService.class })
@TestPropertySource(properties = {
        "jwt.secret=dGhpcy1pcy1hLXZlcnktc2VjdXJlLWtleS1mb3Itand0LXRva2VuLWdlbmVyYXRpb24tMjU2Yml0cw=="
})
@DisplayName("JwtVerificationService Unit Tests")
class JwtVerificationServiceTest {

    private static final String SAME_SECRET_BASE64 = "dGhpcy1pcy1hLXZlcnktc2VjdXJlLWtleS1mb3Itand0LXRva2VuLWdlbmVyYXRpb24tMjU2Yml0cw==";
    private static final String WRONG_SECRET_BASE64 = "YW5vdGhlci1zZWNyZXQta2V5LWZvci10ZXN0aW5nLTI1NmJpdHM=";

    @Autowired
    private JwtVerificationService jwtVerificationService;

    private SecretKey sameKey;
    private SecretKey wrongKey;

    @BeforeEach
    void setUp() {
        sameKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SAME_SECRET_BASE64));
        wrongKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(WRONG_SECRET_BASE64));
    }

    @Test
    @DisplayName("verifyAndGetClaims - valid token - returns claims")
    void verifyAndGetClaims_validToken_returnsClaims() {
        String token = Jwts.builder()
                .subject("testuser")
                .claim("roles", List.of("DOCTOR", "ADMIN"))
                .claim("userId", 100L)
                .claim("staffId", 5L)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600_000))
                .signWith(sameKey, Jwts.SIG.HS256)
                .compact();

        Optional<JwtClaims> result = jwtVerificationService.verifyAndGetClaims(token);

        assertThat(result).isPresent();
        assertThat(result.get().username()).isEqualTo("testuser");
        assertThat(result.get().roles()).containsExactlyInAnyOrder("DOCTOR", "ADMIN");
        assertThat(result.get().userId()).isEqualTo(100L);
        assertThat(result.get().staffId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("verifyAndGetClaims - expired token - returns empty")
    void verifyAndGetClaims_expiredToken_returnsEmpty() {
        String token = Jwts.builder()
                .subject("testuser")
                .claim("roles", List.of("USER"))
                .issuedAt(new Date(System.currentTimeMillis() - 7200_000))
                .expiration(new Date(System.currentTimeMillis() - 3600_000))
                .signWith(sameKey, Jwts.SIG.HS256)
                .compact();

        Optional<JwtClaims> result = jwtVerificationService.verifyAndGetClaims(token);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("verifyAndGetClaims - malformed token - returns empty")
    void verifyAndGetClaims_malformedToken_returnsEmpty() {
        Optional<JwtClaims> result = jwtVerificationService.verifyAndGetClaims("not.a.valid.jwt.token");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("verifyAndGetClaims - token signed with wrong secret - returns empty")
    void verifyAndGetClaims_wrongSecret_returnsEmpty() {
        String token = Jwts.builder()
                .subject("testuser")
                .claim("roles", List.of("USER"))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600_000))
                .signWith(wrongKey, Jwts.SIG.HS256)
                .compact();

        Optional<JwtClaims> result = jwtVerificationService.verifyAndGetClaims(token);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("verifyAndGetClaims - null token - returns empty")
    void verifyAndGetClaims_nullToken_returnsEmpty() {
        Optional<JwtClaims> result = jwtVerificationService.verifyAndGetClaims(null);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("verifyAndGetClaims - blank token - returns empty")
    void verifyAndGetClaims_blankToken_returnsEmpty() {
        assertThat(jwtVerificationService.verifyAndGetClaims("")).isEmpty();
        assertThat(jwtVerificationService.verifyAndGetClaims("   ")).isEmpty();
    }
}
