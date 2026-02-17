package com.hospital.staff.repository;

import com.hospital.staff.model.Specialty;
import com.hospital.staff.model.Staff;
import com.hospital.staff.model.StaffRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                          STAFF REPOSITORY                                    ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS INTERFACE EXISTS:                                                  ║
 * ║  Data access layer for Staff entities.                                       ║
 * ║                                                                              ║
 * ║  Students: Add custom query methods based on your specialized needs.         ║
 * ║  // Business logic will be added in the specialized subject                  ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {

    /**
     * Finds staff by employee ID.
     */
    Optional<Staff> findByEmployeeId(String employeeId);

    /**
     * Finds staff by email.
     */
    Optional<Staff> findByEmail(String email);

    /**
     * Finds all staff by role.
     * WHY: Common query to list all doctors, nurses, etc.
     */
    List<Staff> findByRole(StaffRole role);

    /**
     * Finds all active staff by role.
     */
    List<Staff> findByRoleAndActiveTrue(StaffRole role);

    /**
     * Finds all doctors by specialty.
     * // Business logic will be added in the specialized subject
     */
    List<Staff> findBySpecialtyAndActiveTrue(Specialty specialty);

    /**
     * Finds staff by department.
     */
    List<Staff> findByDepartmentAndActiveTrue(String department);

    /**
     * Checks if employee ID exists.
     */
    boolean existsByEmployeeId(String employeeId);

    /**
     * Custom query: Find available doctors by specialty.
     * Students: This is a placeholder - implement availability logic.
     */
    @Query("SELECT s FROM Staff s WHERE s.role = 'DOCTOR' AND s.specialty = :specialty AND s.active = true")
    List<Staff> findAvailableDoctorsBySpecialty(@Param("specialty") Specialty specialty);
}

