package com.hospital.auth.exception;

/**
 * Thrown when the account is temporarily locked due to too many failed login attempts.
 */
public class AccountTemporarilyLockedException extends RuntimeException {

    public AccountTemporarilyLockedException(String message) {
        super(message);
    }
}
