package com.hospital.auth.controller;

import com.hospital.auth.dto.AuthResponse;
import com.hospital.auth.dto.LoginRequest;
import com.hospital.auth.dto.RegisterRequest;
import com.hospital.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                        AUTH REST CONTROLLER                                  ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS CLASS EXISTS:                                                      ║
 * ║  Exposes REST API endpoints for authentication.                              ║
 * ║                                                                              ║
 * ║  This endpoint is mandatory according to the Kit Commun                      ║
 * ║                                                                              ║
 * ║  Base URL: /api/auth                                                         ║
 * ║                                                                              ║
 * ║  // Security will be reinforced in Subject 3                                 ║
 * ║  Students: This is a SKELETON - implement full security in Subject 3.        ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * Registers a new user.
     * This endpoint is mandatory according to the Kit Commun.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("REST request to register user: {}", request.getUsername());
        AuthResponse response = authService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Authenticates a user.
     * This endpoint is mandatory according to the Kit Commun.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("REST request to login user: {}", request.getUsername());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Refreshes access token.
     * // Security will be reinforced in Subject 3
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestHeader("Refresh-Token") String refreshToken) {
        log.info("REST request to refresh token");
        AuthResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }

    /**
     * Validates a token (internal use).
     * // Security will be reinforced in Subject 3
     */
    @GetMapping("/validate")
    public ResponseEntity<Boolean> validateToken(@RequestHeader("Authorization") String authHeader) {
        log.debug("REST request to validate token");
        String token = authHeader.replace("Bearer ", "");
        boolean isValid = authService.validateToken(token);
        return ResponseEntity.ok(isValid);
    }
}

