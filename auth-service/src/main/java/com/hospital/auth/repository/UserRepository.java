package com.hospital.auth.repository;

import com.hospital.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                          USER REPOSITORY                                     ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS INTERFACE EXISTS:                                                  ║
 * ║  Data access layer for User entities.                                        ║
 * ║                                                                              ║
 * ║  // Security will be reinforced in Subject 3                                 ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByStaffId(Long staffId);
}

