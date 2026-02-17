package com.hospital.staff.service.impl;

import com.hospital.staff.dto.StaffDTO;
import com.hospital.staff.exception.DuplicateStaffException;
import com.hospital.staff.exception.StaffNotFoundException;
import com.hospital.staff.mapper.StaffMapper;
import com.hospital.staff.model.Specialty;
import com.hospital.staff.model.Staff;
import com.hospital.staff.model.StaffRole;
import com.hospital.staff.repository.StaffRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StaffServiceImpl Unit Tests")
class StaffServiceImplTest {

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private StaffMapper staffMapper;

    @InjectMocks
    private StaffServiceImpl staffService;

    private StaffDTO staffDTO;
    private Staff staff;

    @BeforeEach
    void setUp() {
        staffDTO = StaffDTO.builder()
                .id(1L)
                .employeeId("EMP001")
                .firstName("John")
                .lastName("Doctor")
                .email("john.doctor@example.com")
                .phoneNumber("1234567890")
                .role(StaffRole.DOCTOR)
                .specialty(Specialty.CARDIOLOGY)
                .active(true)
                .build();

        staff = Staff.builder()
                .id(1L)
                .employeeId("EMP001")
                .firstName("John")
                .lastName("Doctor")
                .email("john.doctor@example.com")
                .phoneNumber("1234567890")
                .role(StaffRole.DOCTOR)
                .specialty(Specialty.CARDIOLOGY)
                .active(true)
                .build();
    }

    @Test
    @DisplayName("createStaff - valid request - returns StaffDTO")
    void createStaff_validRequest_returnsStaffDTO() {
        // Given
        when(staffRepository.existsByEmployeeId("EMP001")).thenReturn(false);
        when(staffMapper.toEntity(staffDTO)).thenReturn(staff);
        when(staffRepository.save(any(Staff.class))).thenReturn(staff);
        when(staffMapper.toDTO(staff)).thenReturn(staffDTO);

        // When
        StaffDTO result = staffService.createStaff(staffDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmployeeId()).isEqualTo("EMP001");
        verify(staffRepository).existsByEmployeeId("EMP001");
        verify(staffMapper).toEntity(staffDTO);
        verify(staffRepository).save(any(Staff.class));
        verify(staffMapper).toDTO(staff);
    }

    @Test
    @DisplayName("createStaff - duplicate employee ID - throws DuplicateStaffException")
    void createStaff_duplicateEmployeeId_throwsDuplicateStaffException() {
        // Given
        when(staffRepository.existsByEmployeeId("EMP001")).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> staffService.createStaff(staffDTO))
                .isInstanceOf(DuplicateStaffException.class)
                .hasMessageContaining("already exists");
        verify(staffRepository).existsByEmployeeId("EMP001");
        verify(staffMapper, never()).toEntity(any());
        verify(staffRepository, never()).save(any());
    }

    @Test
    @DisplayName("createStaff - sets active to true")
    void createStaff_setsActiveToTrue() {
        // Given
        staffDTO.setActive(false); // Initially false
        when(staffRepository.existsByEmployeeId("EMP001")).thenReturn(false);
        when(staffMapper.toEntity(staffDTO)).thenReturn(staff);
        when(staffRepository.save(any(Staff.class))).thenAnswer(invocation -> {
            Staff savedStaff = invocation.getArgument(0);
            assertThat(savedStaff.isActive()).isTrue();
            return savedStaff;
        });
        when(staffMapper.toDTO(any(Staff.class))).thenReturn(staffDTO);

        // When
        staffService.createStaff(staffDTO);

        // Then
        verify(staffRepository).save(any(Staff.class));
    }

    @Test
    @DisplayName("getStaffById - staff found - returns Optional with StaffDTO")
    void getStaffById_staffFound_returnsOptionalWithStaffDTO() {
        // Given
        when(staffRepository.findById(1L)).thenReturn(Optional.of(staff));
        when(staffMapper.toDTO(staff)).thenReturn(staffDTO);

        // When
        Optional<StaffDTO> result = staffService.getStaffById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        verify(staffRepository).findById(1L);
        verify(staffMapper).toDTO(staff);
    }

    @Test
    @DisplayName("getStaffById - staff not found - returns empty Optional")
    void getStaffById_staffNotFound_returnsEmptyOptional() {
        // Given
        when(staffRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        Optional<StaffDTO> result = staffService.getStaffById(1L);

        // Then
        assertThat(result).isEmpty();
        verify(staffRepository).findById(1L);
        verify(staffMapper, never()).toDTO(any());
    }

    @Test
    @DisplayName("getStaffByEmployeeId - staff found - returns Optional with StaffDTO")
    void getStaffByEmployeeId_staffFound_returnsOptionalWithStaffDTO() {
        // Given
        when(staffRepository.findByEmployeeId("EMP001")).thenReturn(Optional.of(staff));
        when(staffMapper.toDTO(staff)).thenReturn(staffDTO);

        // When
        Optional<StaffDTO> result = staffService.getStaffByEmployeeId("EMP001");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmployeeId()).isEqualTo("EMP001");
        verify(staffRepository).findByEmployeeId("EMP001");
        verify(staffMapper).toDTO(staff);
    }

    @Test
    @DisplayName("getStaffByEmployeeId - staff not found - returns empty Optional")
    void getStaffByEmployeeId_staffNotFound_returnsEmptyOptional() {
        // Given
        when(staffRepository.findByEmployeeId("EMP001")).thenReturn(Optional.empty());

        // When
        Optional<StaffDTO> result = staffService.getStaffByEmployeeId("EMP001");

        // Then
        assertThat(result).isEmpty();
        verify(staffRepository).findByEmployeeId("EMP001");
        verify(staffMapper, never()).toDTO(any());
    }

    @Test
    @DisplayName("getAllStaff - with results - returns list of StaffDTO")
    void getAllStaff_withResults_returnsListOfStaffDTO() {
        // Given
        List<Staff> staffList = List.of(staff);
        when(staffRepository.findAll()).thenReturn(staffList);
        when(staffMapper.toDTO(staff)).thenReturn(staffDTO);

        // When
        List<StaffDTO> result = staffService.getAllStaff();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        verify(staffRepository).findAll();
    }

    @Test
    @DisplayName("getAllStaff - empty results - returns empty list")
    void getAllStaff_emptyResults_returnsEmptyList() {
        // Given
        when(staffRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<StaffDTO> result = staffService.getAllStaff();

        // Then
        assertThat(result).isEmpty();
        verify(staffRepository).findAll();
    }

    @Test
    @DisplayName("getStaffByRole - with results - returns filtered list")
    void getStaffByRole_withResults_returnsFilteredList() {
        // Given
        List<Staff> staffList = List.of(staff);
        when(staffRepository.findByRoleAndActiveTrue(StaffRole.DOCTOR)).thenReturn(staffList);
        when(staffMapper.toDTO(staff)).thenReturn(staffDTO);

        // When
        List<StaffDTO> result = staffService.getStaffByRole(StaffRole.DOCTOR);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRole()).isEqualTo(StaffRole.DOCTOR);
        verify(staffRepository).findByRoleAndActiveTrue(StaffRole.DOCTOR);
    }

    @Test
    @DisplayName("getStaffByRole - empty results - returns empty list")
    void getStaffByRole_emptyResults_returnsEmptyList() {
        // Given
        when(staffRepository.findByRoleAndActiveTrue(StaffRole.NURSE))
                .thenReturn(Collections.emptyList());

        // When
        List<StaffDTO> result = staffService.getStaffByRole(StaffRole.NURSE);

        // Then
        assertThat(result).isEmpty();
        verify(staffRepository).findByRoleAndActiveTrue(StaffRole.NURSE);
    }

    @Test
    @DisplayName("getDoctorsBySpecialty - with results - returns filtered list")
    void getDoctorsBySpecialty_withResults_returnsFilteredList() {
        // Given
        List<Staff> staffList = List.of(staff);
        when(staffRepository.findBySpecialtyAndActiveTrue(Specialty.CARDIOLOGY))
                .thenReturn(staffList);
        when(staffMapper.toDTO(staff)).thenReturn(staffDTO);

        // When
        List<StaffDTO> result = staffService.getDoctorsBySpecialty(Specialty.CARDIOLOGY);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSpecialty()).isEqualTo(Specialty.CARDIOLOGY);
        verify(staffRepository).findBySpecialtyAndActiveTrue(Specialty.CARDIOLOGY);
    }

    @Test
    @DisplayName("getDoctorsBySpecialty - empty results - returns empty list")
    void getDoctorsBySpecialty_emptyResults_returnsEmptyList() {
        // Given
        when(staffRepository.findBySpecialtyAndActiveTrue(Specialty.NEUROLOGY))
                .thenReturn(Collections.emptyList());

        // When
        List<StaffDTO> result = staffService.getDoctorsBySpecialty(Specialty.NEUROLOGY);

        // Then
        assertThat(result).isEmpty();
        verify(staffRepository).findBySpecialtyAndActiveTrue(Specialty.NEUROLOGY);
    }

    @Test
    @DisplayName("updateStaff - staff found - returns updated StaffDTO")
    void updateStaff_staffFound_returnsUpdatedStaffDTO() {
        // Given
        StaffDTO updateDTO = StaffDTO.builder()
                .id(1L)
                .firstName("Jane")
                .lastName("Doctor")
                .email("jane.doctor@example.com")
                .build();

        when(staffRepository.findById(1L)).thenReturn(Optional.of(staff));
        when(staffRepository.save(staff)).thenReturn(staff);
        when(staffMapper.toDTO(staff)).thenReturn(updateDTO);

        // When
        StaffDTO result = staffService.updateStaff(1L, updateDTO);

        // Then
        assertThat(result).isNotNull();
        verify(staffRepository).findById(1L);
        verify(staffMapper).updateEntityFromDTO(updateDTO, staff);
        verify(staffRepository).save(staff);
        verify(staffMapper).toDTO(staff);
    }

    @Test
    @DisplayName("updateStaff - staff not found - throws StaffNotFoundException")
    void updateStaff_staffNotFound_throwsStaffNotFoundException() {
        // Given
        StaffDTO updateDTO = StaffDTO.builder().id(1L).build();
        when(staffRepository.findById(1L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> staffService.updateStaff(1L, updateDTO))
                .isInstanceOf(StaffNotFoundException.class)
                .hasMessageContaining("Staff not found");
        verify(staffRepository).findById(1L);
        verify(staffRepository, never()).save(any());
    }

    @Test
    @DisplayName("deactivateStaff - staff found - deactivates staff")
    void deactivateStaff_staffFound_deactivatesStaff() {
        // Given
        when(staffRepository.findById(1L)).thenReturn(Optional.of(staff));
        when(staffRepository.save(staff)).thenReturn(staff);

        // When
        staffService.deactivateStaff(1L);

        // Then
        assertThat(staff.isActive()).isFalse();
        verify(staffRepository).findById(1L);
        verify(staffRepository).save(staff);
    }

    @Test
    @DisplayName("deactivateStaff - staff not found - throws StaffNotFoundException")
    void deactivateStaff_staffNotFound_throwsStaffNotFoundException() {
        // Given
        when(staffRepository.findById(1L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> staffService.deactivateStaff(1L))
                .isInstanceOf(StaffNotFoundException.class)
                .hasMessageContaining("Staff not found");
        verify(staffRepository).findById(1L);
        verify(staffRepository, never()).save(any());
    }

    @Test
    @DisplayName("existsById - staff exists - returns true")
    void existsById_staffExists_returnsTrue() {
        // Given
        when(staffRepository.existsById(1L)).thenReturn(true);

        // When
        boolean result = staffService.existsById(1L);

        // Then
        assertThat(result).isTrue();
        verify(staffRepository).existsById(1L);
    }

    @Test
    @DisplayName("existsById - staff does not exist - returns false")
    void existsById_staffDoesNotExist_returnsFalse() {
        // Given
        when(staffRepository.existsById(1L)).thenReturn(false);

        // When
        boolean result = staffService.existsById(1L);

        // Then
        assertThat(result).isFalse();
        verify(staffRepository).existsById(1L);
    }
}





