package com.hospital.auth.exception;

/**
 * Thrown when login credentials (username/password) are invalid.
 * Maps to HTTP 401 for anti-bruteforce counting in the gateway.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid credentials");
    }

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
