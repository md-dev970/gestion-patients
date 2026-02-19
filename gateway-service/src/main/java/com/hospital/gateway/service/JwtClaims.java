package com.hospital.gateway.service;

import java.util.List;

/**
 * Claims extracted from a valid JWT (aligned with auth-service token structure).
 * Used to forward user identity to downstream services via headers.
 */
public record JwtClaims(
        String username,
        List<String> roles,
        Long userId,
        Long staffId
) {}
