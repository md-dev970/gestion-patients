package com.hospital.appointment.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Fallback for Staff Service calls.
 */
@Component
@Slf4j
public class StaffClientFallback implements StaffClient {

    @Override
    public Boolean checkStaffExists(Long staffId) {
        log.warn("Staff Service unavailable. Fallback triggered for staff ID: {}", staffId);
        return true;
    }
}

