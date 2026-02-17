package com.hospital.auth.service;

import com.hospital.auth.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service JWT pour la génération et validation des tokens.
 * Implémentation complète selon le Kit Commun.
 */
@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    /**
     * Extrait le username du token JWT.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrait une claim spécifique du token.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Génère un access token pour un utilisateur.
     */
    public String generateToken(User user) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("roles", user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toList()));
        extraClaims.put("userId", user.getId());
        if (user.getStaffId() != null) {
            extraClaims.put("staffId", user.getStaffId());
        }
        return generateToken(extraClaims, user);
    }

    /**
     * Génère un token avec des claims supplémentaires.
     */
    public String generateToken(Map<String, Object> extraClaims, User user) {
        return buildToken(extraClaims, user, jwtExpiration);
    }

    /**
     * Génère un refresh token.
     */
    public String generateRefreshToken(User user) {
        return buildToken(new HashMap<>(), user, refreshExpiration);
    }

    /**
     * Construit le token JWT.
     */
    private String buildToken(Map<String, Object> extraClaims, User user, long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(user.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Valide un token JWT.
     */
    public boolean isTokenValid(String token, User user) {
        try {
            final String username = extractUsername(token);
            return (username.equals(user.getUsername())) && !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Valide un token sans vérifier l'utilisateur (pour le gateway).
     */
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("JWT validation error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Vérifie si le token est expiré.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extrait la date d'expiration du token.
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrait toutes les claims du token.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Obtient la clé de signature.
     */
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Retourne la durée d'expiration du token en secondes.
     */
    public long getExpirationTime() {
        return jwtExpiration / 1000;
    }
}
