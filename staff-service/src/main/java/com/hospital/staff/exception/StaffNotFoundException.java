package com.hospital.staff.exception;

/**
 * Exception thrown when a staff member is not found.
 */
public class StaffNotFoundException extends RuntimeException {
    public StaffNotFoundException(String message) {
        super(message);
    }
}

