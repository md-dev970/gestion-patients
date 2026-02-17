package com.hospital.appointment.model;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                      APPOINTMENT STATUS ENUMERATION                          ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS ENUM EXISTS:                                                       ║
 * ║  Tracks the lifecycle of an appointment.                                     ║
 * ║                                                                              ║
 * ║  Students: Status transitions should be validated in the service layer.      ║
 * ║  // Business logic will be added in the specialized subject                  ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public enum AppointmentStatus {
    /**
     * Appointment is scheduled and confirmed.
     */
    SCHEDULED,
    
    /**
     * Patient has checked in for the appointment.
     */
    CHECKED_IN,
    
    /**
     * Appointment is currently in progress.
     */
    IN_PROGRESS,
    
    /**
     * Appointment has been completed.
     */
    COMPLETED,
    
    /**
     * Appointment was cancelled by patient or staff.
     */
    CANCELLED,
    
    /**
     * Patient did not show up.
     */
    NO_SHOW
}

