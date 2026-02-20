package com.hospital.gateway.service;

import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * T1.3: Verifies JWT with RS256 using public key from config (e.g. Secrets).
 */
@SpringBootTest(classes = { JwtVerificationService.class })
@DisplayName("JwtVerificationService RS256 (public key) Unit Tests")
class JwtVerificationServiceRs256Test {

    private static KeyPair keyPair;
    private static String publicKeyPem;

    @BeforeAll
    static void generateKeyPair() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        keyPair = gen.generateKeyPair();
        publicKeyPem = toPem(keyPair.getPublic());
    }

    @DynamicPropertySource
    static void configureJwtPublicKey(DynamicPropertyRegistry registry) {
        registry.add("jwt.public-key", () -> publicKeyPem);
        registry.add("jwt.secret", () -> "");
    }

    @Autowired
    private JwtVerificationService jwtVerificationService;

    private static String toPem(PublicKey key) {
        String b64 = Base64.getEncoder().encodeToString(key.getEncoded());
        return "-----BEGIN PUBLIC KEY-----\n" + b64 + "\n-----END PUBLIC KEY-----";
    }

    @Test
    @DisplayName("verifyAndGetClaims - RS256 token - returns claims when public key is set")
    void verifyAndGetClaims_rs256Token_returnsClaims() {
        String token = Jwts.builder()
                .subject("testuser")
                .claim("roles", List.of("DOCTOR", "ADMIN"))
                .claim("userId", 100L)
                .claim("staffId", 5L)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600_000))
                .signWith(keyPair.getPrivate(), Jwts.SIG.RS256)
                .compact();

        Optional<JwtClaims> result = jwtVerificationService.verifyAndGetClaims(token);

        assertThat(result).isPresent();
        assertThat(result.get().username()).isEqualTo("testuser");
        assertThat(result.get().roles()).containsExactlyInAnyOrder("DOCTOR", "ADMIN");
        assertThat(result.get().userId()).isEqualTo(100L);
        assertThat(result.get().staffId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("verifyAndGetClaims - RS256 token signed with other key - returns empty")
    void verifyAndGetClaims_rs256WrongKey_returnsEmpty() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair otherPair = gen.generateKeyPair();

        String token = Jwts.builder()
                .subject("testuser")
                .claim("roles", List.of("USER"))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600_000))
                .signWith(otherPair.getPrivate(), Jwts.SIG.RS256)
                .compact();

        Optional<JwtClaims> result = jwtVerificationService.verifyAndGetClaims(token);

        assertThat(result).isEmpty();
    }
}
