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
 * ║                      PATIENT CREATE REQUEST DTO                              ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS CLASS EXISTS:                                                      ║
 * ║  Separate DTO for patient creation requests.                                 ║
 * ║                                                                              ║
 * ║  WHY separate from PatientDTO?                                               ║
 * ║    - Create requests don't include 'id' (server generates it)                ║
 * ║    - Different validation rules may apply                                    ║
 * ║    - Clear separation of concerns                                            ║
 * ║                                                                              ║
 * ║  Students: This pattern is called "Command/Query Separation".                ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientCreateRequest {

    @NotBlank(message = "National ID is required")
    private String nationalId;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50)
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

