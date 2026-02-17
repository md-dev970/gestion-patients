package com.hospital.auth.service;

import com.hospital.auth.dto.AuthResponse;
import com.hospital.auth.dto.LoginRequest;
import com.hospital.auth.dto.RegisterRequest;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                        AUTH SERVICE INTERFACE                                ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS INTERFACE EXISTS:                                                  ║
 * ║  Defines the contract for authentication operations.                         ║
 * ║                                                                              ║
 * ║  // Security will be reinforced in Subject 3                                 ║
 * ║  Students: This is a SKELETON - implement full security in Subject 3.        ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public interface AuthService {

    /**
     * Registers a new user.
     * This endpoint is mandatory according to the Kit Commun.
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Authenticates a user and returns JWT tokens.
     * This endpoint is mandatory according to the Kit Commun.
     */
    AuthResponse login(LoginRequest request);

    /**
     * Refreshes an access token using a refresh token.
     */
    AuthResponse refreshToken(String refreshToken);

    /**
     * Validates a JWT token.
     * // Security will be reinforced in Subject 3
     */
    boolean validateToken(String token);
}

