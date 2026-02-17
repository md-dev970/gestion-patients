package com.hospital.patient.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                      GLOBAL EXCEPTION HANDLER                                ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS CLASS EXISTS:                                                      ║
 * ║  Centralized exception handling for the entire service.                      ║
 * ║  Converts exceptions to proper HTTP responses.                               ║
 * ║                                                                              ║
 * ║  @RestControllerAdvice: Applies to all controllers in this service           ║
 * ║                                                                              ║
 * ║  Students: Add handlers for new exception types as needed.                   ║
 * ║  // Business logic will be added in the specialized subject                  ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handles PatientNotFoundException.
     * Returns HTTP 404 Not Found.
     */
    @ExceptionHandler(PatientNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePatientNotFound(PatientNotFoundException ex) {
        log.error("Patient not found: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(ex.getMessage())
                .build();
        
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles DuplicatePatientException.
     * Returns HTTP 409 Conflict.
     */
    @ExceptionHandler(DuplicatePatientException.class)
    public ResponseEntity<ErrorResponse> handleDuplicatePatient(DuplicatePatientException ex) {
        log.error("Duplicate patient: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Conflict")
                .message(ex.getMessage())
                .build();
        
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    /**
     * Handles validation errors.
     * Returns HTTP 400 Bad Request with field-level errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        log.error("Validation error: {}", ex.getMessage());
        
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });
        
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Error")
                .message("Invalid input data")
                .fieldErrors(fieldErrors)
                .build();
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles all other exceptions.
     * Returns HTTP 500 Internal Server Error.
     * 
     * // Security will be reinforced in Subject 3
     * WHY: Never expose internal errors to clients in production.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error: ", ex);
        
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred. Please try again later.")
                .build();
        
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

