package com.hospital.appointment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                           STAFF FEIGN CLIENT                                 ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS INTERFACE EXISTS:                                                  ║
 * ║  Enables this service to call the Staff Service.                             ║
 * ║                                                                              ║
 * ║  Students: Use this to validate doctor IDs and get doctor availability.      ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
@FeignClient(name = "staff-service", fallback = StaffClientFallback.class)
public interface StaffClient {

    /**
     * Checks if a staff member exists.
     */
    @GetMapping("/api/staff/{id}/exists")
    Boolean checkStaffExists(@PathVariable("id") Long staffId);
}

