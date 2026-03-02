package com.hospital.auth.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GlobalExceptionHandler Unit Tests")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("handleInvalidCredentials - returns 401 with error message")
    void handleInvalidCredentials_returns401() {
        InvalidCredentialsException ex = new InvalidCredentialsException();

        ResponseEntity<Map<String, String>> response = handler.handleInvalidCredentials(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).containsEntry("error", "Invalid credentials");
    }

    @Test
    @DisplayName("handleInvalidCredentials - custom message - returns 401 with custom message")
    void handleInvalidCredentials_customMessage_returns401WithMessage() {
        InvalidCredentialsException ex = new InvalidCredentialsException("Bad credentials");

        ResponseEntity<Map<String, String>> response = handler.handleInvalidCredentials(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).containsEntry("error", "Bad credentials");
    }

    @Test
    @DisplayName("handleAccountTemporarilyLocked - returns 423")
    void handleAccountTemporarilyLocked_returns423() {
        AccountTemporarilyLockedException ex = new AccountTemporarilyLockedException("Account temporarily locked");

        ResponseEntity<Map<String, String>> response = handler.handleAccountTemporarilyLocked(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.LOCKED);
        assertThat(response.getBody()).containsEntry("error", "Account temporarily locked");
    }
}
