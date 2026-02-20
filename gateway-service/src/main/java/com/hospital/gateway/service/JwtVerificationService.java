package com.hospital.gateway.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Verifies JWT tokens issued by auth-service.
 * T1.3: Supports public-key verification (RS256) when jwt.public-key or jwt.public-key-location
 * is set (key loaded from Secrets). Otherwise falls back to shared secret (HS256).
 */
@Service
public class JwtVerificationService {

    @Value("${jwt.secret:}")
    private String secretKey;

    @Value("${jwt.public-key:}")
    private String publicKeyPem;

    @Value("${jwt.public-key-location:}")
    private String publicKeyLocation;

    /**
     * Verifies the token and returns extracted claims if valid.
     * Uses RS256 with public key when configured; otherwise HS256 with shared secret.
     */
    public Optional<JwtClaims> verifyAndGetClaims(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        try {
            Claims claims = parseToken(token.trim());
            if (claims == null) {
                return Optional.empty();
            }

            String username = claims.getSubject();
            if (username == null) {
                return Optional.empty();
            }

            @SuppressWarnings("unchecked")
            List<String> roles = claims.get("roles", List.class);
            if (roles == null) {
                roles = Collections.emptyList();
            }
            List<String> rolesList = roles.stream()
                    .map(String::valueOf)
                    .collect(Collectors.toList());

            Long userId = claims.get("userId", Long.class);
            if (userId == null && claims.get("userId") != null) {
                userId = ((Number) claims.get("userId")).longValue();
            }

            Long staffId = null;
            if (claims.get("staffId") != null) {
                Object sid = claims.get("staffId");
                staffId = sid instanceof Number ? ((Number) sid).longValue() : null;
            }

            return Optional.of(new JwtClaims(username, rolesList, userId, staffId));
        } catch (ExpiredJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private Claims parseToken(String token) {
        PublicKey publicKey = resolvePublicKey();
        if (publicKey != null) {
            return Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        }
        if (secretKey != null && !secretKey.isBlank()) {
            SecretKey key = getSignInKey();
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        }
        return null;
    }

    /**
     * Resolves public key from PEM string (jwt.public-key) or file (jwt.public-key-location).
     * Used when key is provided from Secrets (e.g. env JWT_PUBLIC_KEY or mounted file).
     */
    private PublicKey resolvePublicKey() {
        String pem = publicKeyPem;
        if ((pem == null || pem.isBlank()) && publicKeyLocation != null && !publicKeyLocation.isBlank()) {
            try {
                pem = Files.readString(Paths.get(publicKeyLocation), StandardCharsets.UTF_8);
            } catch (Exception e) {
                return null;
            }
        }
        if (pem == null || pem.isBlank()) {
            return null;
        }
        return parsePemPublicKey(pem);
    }

    private static PublicKey parsePemPublicKey(String pem) {
        try {
            String content = pem
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] decoded = Base64.getDecoder().decode(content);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(spec);
        } catch (Exception e) {
            return null;
        }
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
