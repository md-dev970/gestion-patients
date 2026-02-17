package com.hospital.patient.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                           ERROR RESPONSE DTO                                 ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS CLASS EXISTS:                                                      ║
 * ║  Standardized error response format for all API errors.                      ║
 * ║                                                                              ║
 * ║  WHY standardize errors?                                                     ║
 * ║    1. Consistent client experience                                           ║
 * ║    2. Easier error handling in frontend                                      ║
 * ║    3. Better debugging information                                           ║
 * ║                                                                              ║
 * ║  Students: Use this format across ALL microservices.                         ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    /**
     * When the error occurred.
     */
    private LocalDateTime timestamp;

    /**
     * HTTP status code.
     */
    private int status;

    /**
     * Error type (e.g., "Not Found", "Bad Request").
     */
    private String error;

    /**
     * Human-readable error message.
     */
    private String message;

    /**
     * Field-level validation errors (optional).
     * WHY: Allows clients to highlight specific form fields.
     */
    private Map<String, String> fieldErrors;
}

