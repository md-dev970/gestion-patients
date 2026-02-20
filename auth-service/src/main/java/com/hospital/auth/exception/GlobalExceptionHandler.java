package com.hospital.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Maps exceptions to HTTP responses. AccountTemporarilyLockedException → 423 Locked.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountTemporarilyLockedException.class)
    public ResponseEntity<Map<String, String>> handleAccountTemporarilyLocked(AccountTemporarilyLockedException ex) {
        return ResponseEntity
                .status(HttpStatus.LOCKED)
                .body(Map.of("error", ex.getMessage() != null ? ex.getMessage() : "Account temporarily locked"));
    }
}
