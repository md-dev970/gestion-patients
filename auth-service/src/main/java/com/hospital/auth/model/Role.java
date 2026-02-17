package com.hospital.auth.model;

/**
 * Enumération des rôles utilisateur pour le contrôle d'accès (RBAC).
 * Conforme au Kit Commun.
 * 
 * Rôles minimum requis par le cahier des charges:
 *   - ADMIN
 *   - MÉDECIN (DOCTOR)
 *   - INFIRMIER (NURSE)
 *   - PATIENT
 */
public enum Role {
    /**
     * Administrateur système - accès complet.
     */
    ROLE_ADMIN,
    
    /**
     * Médecin - peut accéder aux dossiers patients, créer des consultations.
     */
    ROLE_MEDECIN,
    
    /**
     * Alias anglais pour MEDECIN.
     */
    ROLE_DOCTOR,
    
    /**
     * Infirmier - peut accéder aux dossiers patients avec restrictions.
     */
    ROLE_INFIRMIER,
    
    /**
     * Alias anglais pour INFIRMIER.
     */
    ROLE_NURSE,
    
    /**
     * Réceptionniste - peut gérer les rendez-vous.
     */
    ROLE_RECEPTIONIST,
    
    /**
     * Technicien de laboratoire - peut ajouter des résultats de lab.
     */
    ROLE_LAB_TECH,
    
    /**
     * Patient - peut voir ses propres dossiers.
     */
    ROLE_PATIENT
}
