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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Verifies JWT tokens issued by auth-service (HS256, same secret).
 * Extracts claims for downstream use (username, roles, userId, staffId).
 */
@Service
public class JwtVerificationService {

    @Value("${jwt.secret}")
    private String secretKey;

    /**
     * Verifies the token and returns extracted claims if valid.
     * Same key derivation as auth-service: Base64 decode then HMAC SHA-256 key.
     */
    public Optional<JwtClaims> verifyAndGetClaims(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token.trim())
                    .getPayload();

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

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
