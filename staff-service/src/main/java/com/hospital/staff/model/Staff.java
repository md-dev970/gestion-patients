package com.hospital.staff.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                            STAFF ENTITY                                      ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS CLASS EXISTS:                                                      ║
 * ║  Represents a hospital staff member (doctor, nurse, admin, etc.)             ║
 * ║  This is the core domain entity for the Staff Service.                       ║
 * ║                                                                              ║
 * ║  Students: Add additional fields based on your specialized subject.          ║
 * ║  The fields here are the MINIMUM required by the Kit Commun.                 ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
@Entity
@Table(name = "staff")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique employee identifier.
     * WHY: Internal hospital reference number.
     */
    @Column(name = "employee_id", unique = true, nullable = false)
    private String employeeId;

    /**
     * Staff member's first name.
     * This field is mandatory according to the Kit Commun.
     */
    @Column(name = "first_name", nullable = false)
    private String firstName;

    /**
     * Staff member's last name.
     * This field is mandatory according to the Kit Commun.
     */
    @Column(name = "last_name", nullable = false)
    private String lastName;

    /**
     * Professional email address.
     */
    @Column(unique = true, nullable = false)
    private String email;

    /**
     * Phone number.
     */
    @Column(name = "phone_number")
    private String phoneNumber;

    /**
     * Role/position in the hospital.
     * // Business logic will be added in the specialized subject
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StaffRole role;

    /**
     * Medical specialty (for doctors).
     * // Business logic will be added in the specialized subject
     */
    @Enumerated(EnumType.STRING)
    private Specialty specialty;

    /**
     * Department assignment.
     */
    @Column(name = "department")
    private String department;

    /**
     * Professional license number.
     * WHY: Required for medical staff validation.
     */
    @Column(name = "license_number")
    private String licenseNumber;

    /**
     * Date when the staff member joined.
     */
    @Column(name = "hire_date")
    private LocalDate hireDate;

    /**
     * Whether the staff member is currently active.
     * WHY: Soft delete pattern - inactive staff are not deleted.
     */
    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

