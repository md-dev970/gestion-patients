package com.hospital.appointment.model;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                       APPOINTMENT TYPE ENUMERATION                           ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS ENUM EXISTS:                                                       ║
 * ║  Categorizes appointments for scheduling and billing purposes.               ║
 * ║                                                                              ║
 * ║  Students: Different types may have different durations/rules.               ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public enum AppointmentType {
    INITIAL_CONSULTATION,
    FOLLOW_UP,
    ROUTINE_CHECKUP,
    EMERGENCY,
    SPECIALIST_REFERRAL,
    LAB_WORK,
    IMAGING,
    PROCEDURE
}

