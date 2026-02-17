package com.hospital.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour les requêtes d'inscription utilisateur.
 * Conforme au Kit Commun.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    /**
     * Optional: Link to staff member.
     */
    private Long staffId;

    /**
     * Optional: Role to assign (defaults to ROLE_PATIENT).
     * Values: ROLE_ADMIN, ROLE_DOCTOR, ROLE_NURSE, ROLE_RECEPTIONIST, ROLE_LAB_TECH, ROLE_PATIENT
     */
    private String role;
}
