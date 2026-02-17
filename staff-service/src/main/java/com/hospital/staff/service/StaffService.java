package com.hospital.staff.service;

import com.hospital.staff.dto.StaffDTO;
import com.hospital.staff.model.Specialty;
import com.hospital.staff.model.StaffRole;

import java.util.List;
import java.util.Optional;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                        STAFF SERVICE INTERFACE                               ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS INTERFACE EXISTS:                                                  ║
 * ║  Defines the contract for staff-related business operations.                 ║
 * ║                                                                              ║
 * ║  // Business logic will be added in the specialized subject                  ║
 * ║  Students: Implement the business rules in StaffServiceImpl.                 ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public interface StaffService {

    /**
     * Creates a new staff member.
     * This endpoint is mandatory according to the Kit Commun.
     */
    StaffDTO createStaff(StaffDTO staffDTO);

    /**
     * Retrieves a staff member by ID.
     * This endpoint is mandatory according to the Kit Commun.
     */
    Optional<StaffDTO> getStaffById(Long id);

    /**
     * Retrieves a staff member by employee ID.
     */
    Optional<StaffDTO> getStaffByEmployeeId(String employeeId);

    /**
     * Retrieves all staff members.
     * // Permissions will be checked in Subject 2
     */
    List<StaffDTO> getAllStaff();

    /**
     * Retrieves all active staff by role.
     */
    List<StaffDTO> getStaffByRole(StaffRole role);

    /**
     * Retrieves all doctors by specialty.
     * // Business logic will be added in the specialized subject
     */
    List<StaffDTO> getDoctorsBySpecialty(Specialty specialty);

    /**
     * Updates a staff member.
     * This endpoint is mandatory according to the Kit Commun.
     */
    StaffDTO updateStaff(Long id, StaffDTO staffDTO);

    /**
     * Deactivates a staff member (soft delete).
     * // Permissions will be checked in Subject 2
     */
    void deactivateStaff(Long id);

    /**
     * Checks if a staff member exists.
     */
    boolean existsById(Long id);
}

