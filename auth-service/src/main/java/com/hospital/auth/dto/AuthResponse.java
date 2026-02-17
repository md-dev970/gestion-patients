package com.hospital.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                       AUTHENTICATION RESPONSE DTO                            ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS CLASS EXISTS:                                                      ║
 * ║  DTO for authentication responses containing JWT token.                      ║
 * ║                                                                              ║
 * ║  // Security will be reinforced in Subject 3                                 ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    /**
     * JWT access token.
     */
    private String accessToken;

    /**
     * Refresh token for token renewal.
     */
    private String refreshToken;

    /**
     * Token type (usually "Bearer").
     */
    private String tokenType = "Bearer";

    /**
     * Token expiration time in seconds.
     */
    private Long expiresIn;

    /**
     * User's username.
     */
    private String username;

    /**
     * User's roles.
     */
    private Set<String> roles;
}

