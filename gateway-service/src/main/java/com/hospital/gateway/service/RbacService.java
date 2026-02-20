package com.hospital.gateway.service;

import com.hospital.gateway.rbac.Action;
import com.hospital.gateway.rbac.Resource;
import com.hospital.gateway.rbac.RbacPolicy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Decides if a request (path + HTTP method) is allowed for the given roles
 * on patient-dossier resources. Paths outside that scope are allowed.
 */
@Service
public class RbacService {

    private static final String PREFIX_PATIENTS = "/api/patients";
    private static final String PREFIX_MEDICAL_RECORDS = "/api/medical-records";
    private static final String PREFIX_CONSULTATIONS = "/api/consultations";

    /**
     * Returns true if the request is allowed for at least one of the given roles.
     * For paths not under /api/patients, /api/medical-records, or /api/consultations, returns true (no RBAC).
     */
    public boolean isAllowed(String path, String method, List<String> roles) {
        Optional<Resource> resource = resolveResource(path);
        if (resource.isEmpty()) {
            return true;
        }
        Action action = resolveAction(method);
        Set<String> allowed = RbacPolicy.allowedRoles(resource.get(), action);
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        for (String role : roles) {
            if (role != null && allowed.contains(role.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the resolved resource for the path, or empty if not a patient-dossier path.
     */
    public Optional<Resource> resolveResource(String path) {
        if (path == null) return Optional.empty();
        if (path.startsWith(PREFIX_PATIENTS)) return Optional.of(Resource.PATIENTS);
        if (path.startsWith(PREFIX_MEDICAL_RECORDS)) return Optional.of(Resource.MEDICAL_RECORDS);
        if (path.startsWith(PREFIX_CONSULTATIONS)) return Optional.of(Resource.CONSULTATIONS);
        return Optional.empty();
    }

    /**
     * Resolves HTTP method to action.
     */
    public Action resolveAction(String method) {
        if (method == null) return Action.READ;
        return switch (method.toUpperCase()) {
            case "GET" -> Action.READ;
            case "POST" -> Action.CREATE;
            case "PUT", "PATCH" -> Action.UPDATE;
            case "DELETE" -> Action.DELETE;
            default -> Action.READ;
        };
    }

    /**
     * Extracts a resource ID from path if present (e.g. /api/patients/123 -> "123").
     * Used for audit event resourceId. Returns null if not applicable.
     */
    public String extractResourceId(String path, Resource resource) {
        if (path == null) return null;
        String prefix = switch (resource) {
            case PATIENTS -> PREFIX_PATIENTS;
            case MEDICAL_RECORDS -> PREFIX_MEDICAL_RECORDS;
            case CONSULTATIONS -> PREFIX_CONSULTATIONS;
        };
        String rest = path.length() > prefix.length() ? path.substring(prefix.length()) : "";
        if (!rest.startsWith("/")) return null;
        rest = rest.substring(1).trim();
        if (rest.isEmpty()) return null;
        // First path segment (e.g. 123 or search)
        int slash = rest.indexOf('/');
        String segment = slash >= 0 ? rest.substring(0, slash) : rest;
        return segment.matches("\\d+") ? segment : null;
    }
}
