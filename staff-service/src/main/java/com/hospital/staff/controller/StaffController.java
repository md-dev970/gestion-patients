package com.hospital.staff.controller;

import com.hospital.staff.dto.StaffDTO;
import com.hospital.staff.model.Specialty;
import com.hospital.staff.model.StaffRole;
import com.hospital.staff.service.StaffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                          STAFF REST CONTROLLER                               ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS CLASS EXISTS:                                                      ║
 * ║  Exposes REST API endpoints for staff operations.                            ║
 * ║                                                                              ║
 * ║  This endpoint is mandatory according to the Kit Commun                      ║
 * ║                                                                              ║
 * ║  Base URL: /api/staff                                                        ║
 * ║                                                                              ║
 * ║  // Permissions will be checked in Subject 2                                 ║
 * ║  // Security will be reinforced in Subject 3                                 ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
@Slf4j
public class StaffController {

    private final StaffService staffService;

    /**
     * Creates a new staff member.
     * This endpoint is mandatory according to the Kit Commun.
     */
    @PostMapping
    public ResponseEntity<StaffDTO> createStaff(@Valid @RequestBody StaffDTO staffDTO) {
        log.info("REST request to create staff member");
        StaffDTO createdStaff = staffService.createStaff(staffDTO);
        return new ResponseEntity<>(createdStaff, HttpStatus.CREATED);
    }

    /**
     * Retrieves all staff members.
     * This endpoint is mandatory according to the Kit Commun.
     * // Permissions will be checked in Subject 2
     */
    @GetMapping
    public ResponseEntity<List<StaffDTO>> getAllStaff() {
        log.info("REST request to get all staff");
        List<StaffDTO> staff = staffService.getAllStaff();
        return ResponseEntity.ok(staff);
    }

    /**
     * Retrieves a staff member by ID.
     * This endpoint is mandatory according to the Kit Commun.
     */
    @GetMapping("/{id}")
    public ResponseEntity<StaffDTO> getStaffById(@PathVariable Long id) {
        log.info("REST request to get staff by ID: {}", id);
        return staffService.getStaffById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieves staff by role.
     * // Business logic will be added in the specialized subject
     */
    @GetMapping("/role/{role}")
    public ResponseEntity<List<StaffDTO>> getStaffByRole(@PathVariable StaffRole role) {
        log.info("REST request to get staff by role: {}", role);
        List<StaffDTO> staff = staffService.getStaffByRole(role);
        return ResponseEntity.ok(staff);
    }

    /**
     * Retrieves doctors by specialty.
     * // Business logic will be added in the specialized subject
     */
    @GetMapping("/doctors/specialty/{specialty}")
    public ResponseEntity<List<StaffDTO>> getDoctorsBySpecialty(@PathVariable Specialty specialty) {
        log.info("REST request to get doctors by specialty: {}", specialty);
        List<StaffDTO> doctors = staffService.getDoctorsBySpecialty(specialty);
        return ResponseEntity.ok(doctors);
    }

    /**
     * Updates a staff member.
     * This endpoint is mandatory according to the Kit Commun.
     */
    @PutMapping("/{id}")
    public ResponseEntity<StaffDTO> updateStaff(
            @PathVariable Long id,
            @Valid @RequestBody StaffDTO staffDTO) {
        log.info("REST request to update staff: {}", id);
        StaffDTO updatedStaff = staffService.updateStaff(id, staffDTO);
        return ResponseEntity.ok(updatedStaff);
    }

    /**
     * Deactivates a staff member.
     * // Permissions will be checked in Subject 2
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateStaff(@PathVariable Long id) {
        log.info("REST request to deactivate staff: {}", id);
        staffService.deactivateStaff(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Checks if a staff member exists.
     */
    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> checkStaffExists(@PathVariable Long id) {
        log.debug("REST request to check if staff exists: {}", id);
        boolean exists = staffService.existsById(id);
        return ResponseEntity.ok(exists);
    }
}

