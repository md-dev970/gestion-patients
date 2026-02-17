package com.hospital.patient.dto;

import com.hospital.patient.model.Gender;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                         PATIENT DATA TRANSFER OBJECT                         ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS CLASS EXISTS:                                                      ║
 * ║  DTOs separate the API layer from the persistence layer.                     ║
 * ║                                                                              ║
 * ║  WHY NOT expose entities directly?                                           ║
 * ║    1. Security: Hide internal fields (e.g., createdAt, passwords)            ║
 * ║    2. Flexibility: Change entity without breaking API                        ║
 * ║    3. Validation: Add API-specific validation rules                          ║
 * ║                                                                              ║
 * ║  Students: Add validation annotations based on your requirements.            ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientDTO {

    private Long id;

    @NotBlank(message = "National ID is required")
    private String nationalId;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @Email(message = "Invalid email format")
    private String email;

    private String phoneNumber;

    private String address;

    private String bloodType;

    private String emergencyContactName;

    private String emergencyContactPhone;
}

