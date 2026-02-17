package com.hospital.medicalrecord.dto;

import com.hospital.medicalrecord.model.EntryType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                     MEDICAL ENTRY DATA TRANSFER OBJECT                       ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS CLASS EXISTS:                                                      ║
 * ║  DTO for transferring individual medical entries.                            ║
 * ║                                                                              ║
 * ║  Students: Validate required fields based on entry type.                     ║
 * ║  // Business logic will be added in the specialized subject                  ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalEntryDTO {

    private Long id;

    @NotNull(message = "Entry type is required")
    private EntryType entryType;

    private LocalDateTime entryDate;

    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    private String diagnosis;

    private String symptoms;

    private String treatment;

    private String prescription;

    private String notes;

    private String followUp;

    // Populated from Staff Service
    private String doctorName;
}

