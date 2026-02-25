package com.hospital.gateway.rbac;

import java.util.EnumSet;
import java.util.Set;

/**
 * RBAC engine: role/resource/action rules (T1.4).
 * Role names must match auth-service Role enum (ROLE_ADMIN, ROLE_DOCTOR, etc.).
 * Allow/deny decision is applied in RbacAuthorizationFilter before proxying to backends.
 */
public final class RbacPolicy {

    private static final String ADMIN = "ROLE_ADMIN";
    private static final String MEDECIN = "ROLE_MEDECIN";
    private static final String DOCTOR = "ROLE_DOCTOR";
    private static final String INFIRMIER = "ROLE_INFIRMIER";
    private static final String NURSE = "ROLE_NURSE";
    private static final String RECEPTIONIST = "ROLE_RECEPTIONIST";
    private static final String LAB_TECH = "ROLE_LAB_TECH";
    // ROLE_PATIENT: no access to patient-dossier paths in this matrix

    private RbacPolicy() {}

    /**
     * Returns the set of roles allowed for the given resource and action.
     */
    public static Set<String> allowedRoles(Resource resource, Action action) {
        return switch (resource) {
            case PATIENTS -> allowedForPatients(action);
            case MEDICAL_RECORDS -> allowedForMedicalRecords(action);
            case CONSULTATIONS -> allowedForConsultations(action);
            case APPOINTMENTS -> allowedForAppointments(action);
        };
    }

    private static Set<String> allowedForPatients(Action action) {
        return switch (action) {
            // T6.4: GET /api/patients/{id}/dossier — DOCTOR/MEDECIN allowed, ROLE_PATIENT denied
            case READ -> Set.of(ADMIN, MEDECIN, DOCTOR, INFIRMIER, NURSE, RECEPTIONIST, LAB_TECH);
            case CREATE -> Set.of(ADMIN, MEDECIN, DOCTOR, NURSE, RECEPTIONIST);
            case UPDATE -> Set.of(ADMIN, MEDECIN, DOCTOR, NURSE);
            case DELETE -> Set.of(ADMIN);
        };
    }

    private static Set<String> allowedForMedicalRecords(Action action) {
        return switch (action) {
            case READ -> Set.of(ADMIN, MEDECIN, DOCTOR, INFIRMIER, NURSE, LAB_TECH);
            case CREATE -> Set.of(ADMIN, MEDECIN, DOCTOR, NURSE, LAB_TECH);
            case UPDATE -> Set.of(ADMIN, MEDECIN, DOCTOR, NURSE);
            case DELETE -> Set.of(ADMIN);
        };
    }

    private static Set<String> allowedForConsultations(Action action) {
        return switch (action) {
            case READ -> Set.of(ADMIN, MEDECIN, DOCTOR, INFIRMIER, NURSE);
            case CREATE -> Set.of(ADMIN, MEDECIN, DOCTOR, NURSE);
            case UPDATE -> Set.of(ADMIN, MEDECIN, DOCTOR, NURSE);
            case DELETE -> Set.of(ADMIN);
        };
    }

    private static Set<String> allowedForAppointments(Action action) {
        return switch (action) {
            case READ -> Set.of(ADMIN, MEDECIN, DOCTOR, INFIRMIER, NURSE, RECEPTIONIST);
            case CREATE -> Set.of(ADMIN, MEDECIN, DOCTOR, NURSE, RECEPTIONIST);
            case UPDATE -> Set.of(ADMIN, MEDECIN, DOCTOR, NURSE, RECEPTIONIST);
            case DELETE -> Set.of(ADMIN);  // T6.1: DELETE by patientId ADMIN only
        };
    }
}
