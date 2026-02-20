package com.hospital.auth.service;

import com.hospital.auth.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service JWT pour la génération et validation des tokens.
 * T1.3: When jwt.private-key or jwt.private-key-location is set (from Secrets), signs with RS256;
 * otherwise uses HS256 with shared secret.
 */
@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secret:}")
    private String secretKey;

    @Value("${jwt.private-key:}")
    private String privateKeyPem;

    @Value("${jwt.private-key-location:}")
    private String privateKeyLocation;

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
     * Construit le token JWT (RS256 si clé privée configurée, sinon HS256).
     */
    private String buildToken(Map<String, Object> extraClaims, User user, long expiration) {
        var builder = Jwts.builder()
                .claims(extraClaims)
                .subject(user.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration));
        PrivateKey privateKey = resolvePrivateKey();
        if (privateKey != null) {
            return builder.signWith(privateKey, Jwts.SIG.RS256).compact();
        }
        return builder.signWith(getSignInKey(), Jwts.SIG.HS256).compact();
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
     * Extrait toutes les claims du token (vérification RS256 ou HS256 selon la config).
     */
    private Claims extractAllClaims(String token) {
        PrivateKey privateKey = resolvePrivateKey();
        if (privateKey != null) {
            PublicKey publicKey = derivePublicKey(privateKey);
            if (publicKey != null) {
                return Jwts.parser()
                        .verifyWith(publicKey)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();
            }
        }
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private PrivateKey resolvePrivateKey() {
        String pem = privateKeyPem;
        if ((pem == null || pem.isBlank()) && privateKeyLocation != null && !privateKeyLocation.isBlank()) {
            try {
                pem = Files.readString(Paths.get(privateKeyLocation), StandardCharsets.UTF_8);
            } catch (Exception e) {
                return null;
            }
        }
        if (pem == null || pem.isBlank()) {
            return null;
        }
        return parsePemPrivateKey(pem);
    }

    private static PrivateKey parsePemPrivateKey(String pem) {
        try {
            String content = pem
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] decoded = Base64.getDecoder().decode(content);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(spec);
        } catch (Exception e) {
            return null;
        }
    }

    private static PublicKey derivePublicKey(PrivateKey privateKey) {
        if (!(privateKey instanceof RSAPrivateCrtKey rsaPrivate)) {
            return null;
        }
        try {
            RSAPublicKeySpec publicSpec = new RSAPublicKeySpec(rsaPrivate.getModulus(), rsaPrivate.getPublicExponent());
            return KeyFactory.getInstance("RSA").generatePublic(publicSpec);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Obtient la clé de signature HMAC (HS256).
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
