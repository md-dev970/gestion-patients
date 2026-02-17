package com.hospital.staff.service.impl;

import com.hospital.staff.dto.StaffDTO;
import com.hospital.staff.exception.DuplicateStaffException;
import com.hospital.staff.exception.StaffNotFoundException;
import com.hospital.staff.mapper.StaffMapper;
import com.hospital.staff.model.Specialty;
import com.hospital.staff.model.Staff;
import com.hospital.staff.model.StaffRole;
import com.hospital.staff.repository.StaffRepository;
import com.hospital.staff.service.StaffService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                     STAFF SERVICE IMPLEMENTATION                             ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS CLASS EXISTS:                                                      ║
 * ║  Implements the business logic for staff operations.                         ║
 * ║                                                                              ║
 * ║  // Business logic will be added in the specialized subject                  ║
 * ║  Students: This is where you implement your business rules.                  ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class StaffServiceImpl implements StaffService {

    private final StaffRepository staffRepository;
    private final StaffMapper staffMapper;

    @Override
    public StaffDTO createStaff(StaffDTO staffDTO) {
        log.info("Creating new staff member: {}", staffDTO.getEmployeeId());

        if (staffRepository.existsByEmployeeId(staffDTO.getEmployeeId())) {
            throw new DuplicateStaffException(
                    "Staff with employee ID " + staffDTO.getEmployeeId() + " already exists");
        }

        Staff staff = staffMapper.toEntity(staffDTO);
        staff.setActive(true);
        
        Staff savedStaff = staffRepository.save(staff);
        log.info("Staff created with ID: {}", savedStaff.getId());

        return staffMapper.toDTO(savedStaff);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StaffDTO> getStaffById(Long id) {
        log.debug("Fetching staff by ID: {}", id);
        return staffRepository.findById(id)
                .map(staffMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StaffDTO> getStaffByEmployeeId(String employeeId) {
        log.debug("Fetching staff by employee ID: {}", employeeId);
        return staffRepository.findByEmployeeId(employeeId)
                .map(staffMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffDTO> getAllStaff() {
        log.debug("Fetching all staff");
        // Permissions will be checked in Subject 2
        return staffRepository.findAll().stream()
                .map(staffMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffDTO> getStaffByRole(StaffRole role) {
        log.debug("Fetching staff by role: {}", role);
        return staffRepository.findByRoleAndActiveTrue(role).stream()
                .map(staffMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffDTO> getDoctorsBySpecialty(Specialty specialty) {
        log.debug("Fetching doctors by specialty: {}", specialty);
        // Business logic will be added in the specialized subject
        return staffRepository.findBySpecialtyAndActiveTrue(specialty).stream()
                .map(staffMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public StaffDTO updateStaff(Long id, StaffDTO staffDTO) {
        log.info("Updating staff with ID: {}", id);
        // Permissions will be checked in Subject 2

        Staff existingStaff = staffRepository.findById(id)
                .orElseThrow(() -> new StaffNotFoundException("Staff not found with ID: " + id));

        staffMapper.updateEntityFromDTO(staffDTO, existingStaff);
        Staff updatedStaff = staffRepository.save(existingStaff);
        
        log.info("Staff updated successfully: {}", id);
        return staffMapper.toDTO(updatedStaff);
    }

    @Override
    public void deactivateStaff(Long id) {
        log.info("Deactivating staff with ID: {}", id);
        // Permissions will be checked in Subject 2

        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new StaffNotFoundException("Staff not found with ID: " + id));

        staff.setActive(false);
        staffRepository.save(staff);
        log.info("Staff deactivated successfully: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return staffRepository.existsById(id);
    }
}

