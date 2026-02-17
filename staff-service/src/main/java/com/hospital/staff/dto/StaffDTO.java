package com.hospital.staff.dto;

import com.hospital.staff.model.Specialty;
import com.hospital.staff.model.StaffRole;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                        STAFF DATA TRANSFER OBJECT                            ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS CLASS EXISTS:                                                      ║
 * ║  DTO for transferring staff data between layers.                             ║
 * ║  Separates API contract from persistence model.                              ║
 * ║                                                                              ║
 * ║  Students: Add validation annotations based on your requirements.            ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffDTO {

    private Long id;

    @NotBlank(message = "Employee ID is required")
    private String employeeId;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50)
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String phoneNumber;

    @NotNull(message = "Role is required")
    private StaffRole role;

    private Specialty specialty;

    private String department;

    private String licenseNumber;

    private LocalDate hireDate;

    private boolean active;
}

