package com.hospital.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                          LOGIN REQUEST DTO                                   ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS CLASS EXISTS:                                                      ║
 * ║  DTO for user login requests.                                                ║
 * ║                                                                              ║
 * ║  // Security will be reinforced in Subject 3                                 ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}

