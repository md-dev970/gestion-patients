package com.hospital.auth.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                             USER ENTITY                                      ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS CLASS EXISTS:                                                      ║
 * ║  Represents a system user for authentication purposes.                       ║
 * ║                                                                              ║
 * ║  NOTE: This is separate from Staff/Patient entities.                         ║
 * ║  Users are linked to Staff via staffId for authorization.                    ║
 * ║                                                                              ║
 * ║  // Security will be reinforced in Subject 3                                 ║
 * ║  Students: Implement proper password hashing and security.                   ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Username for login.
     */
    @Column(unique = true, nullable = false)
    private String username;

    /**
     * Email address.
     */
    @Column(unique = true, nullable = false)
    private String email;

    /**
     * Hashed password.
     * // Security will be reinforced in Subject 3
     * WHY: NEVER store plain text passwords!
     */
    @Column(nullable = false)
    private String password;

    /**
     * Reference to Staff entity (if applicable).
     * WHY: Links authentication to staff member for authorization.
     */
    @Column(name = "staff_id")
    private Long staffId;

    /**
     * User roles for authorization.
     * // Permissions will be checked in Subject 2
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    /**
     * Whether the account is active.
     */
    @Column(nullable = false)
    private boolean enabled = true;

    /**
     * Whether the account is locked.
     */
    @Column(name = "account_non_locked")
    private boolean accountNonLocked = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

