package com.hospital.staff.exception;

/**
 * Exception thrown when attempting to create a duplicate staff member.
 */
public class DuplicateStaffException extends RuntimeException {
    public DuplicateStaffException(String message) {
        super(message);
    }
}

