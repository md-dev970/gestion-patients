package com.hospital.auth.service.impl;

import com.hospital.auth.audit.SecurityAuditSender;
import com.hospital.auth.config.BruteforceProperties;
import com.hospital.auth.dto.AuthResponse;
import com.hospital.auth.dto.LoginRequest;
import com.hospital.auth.dto.RegisterRequest;
import com.hospital.auth.exception.AccountTemporarilyLockedException;
import com.hospital.auth.model.Role;
import com.hospital.auth.model.User;
import com.hospital.auth.repository.UserRepository;
import com.hospital.auth.service.AuthService;
import com.hospital.auth.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implémentation complète du service d'authentification avec JWT.
 * Conforme au Kit Commun - Microservice Auth.
 * 
 * Fonctionnalités:
 *   - Authentification par login/mot de passe
 *   - Génération de JWT
 *   - Vérification de token
 *   - Gestion de session (expiration)
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final BruteforceProperties bruteforceProperties;
    private final SecurityAuditSender securityAuditSender;

    @Override
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getUsername());

        // Vérification des doublons
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Déterminer le rôle en fonction de la requête
        Set<Role> roles = Set.of(Role.ROLE_PATIENT); // Rôle par défaut
        if (request.getRole() != null) {
            try {
                roles = Set.of(Role.valueOf(request.getRole()));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid role specified: {}, defaulting to ROLE_PATIENT", request.getRole());
            }
        }

        // Créer l'utilisateur avec mot de passe hashé
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .staffId(request.getStaffId())
                .roles(roles)
                .enabled(true)
                .accountNonLocked(true)
                .failedAttempts(0)
                .lockoutEnd(null)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered with ID: {}", savedUser.getId());

        // Générer les tokens JWT
        String accessToken = jwtService.generateToken(savedUser);
        String refreshToken = jwtService.generateRefreshToken(savedUser);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime())
                .username(savedUser.getUsername())
                .roles(savedUser.getRoles().stream()
                        .map(Enum::name)
                        .collect(Collectors.toSet()))
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check existing lock: unlock if lockout has expired
        if (!user.isAccountNonLocked() && user.getLockoutEnd() != null) {
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(user.getLockoutEnd())) {
                throw new AccountTemporarilyLockedException("Account temporarily locked");
            }
            // Lock expired: unlock and continue
            user.setFailedAttempts(0);
            user.setLockoutEnd(null);
            user.setAccountNonLocked(true);
            userRepository.save(user);
        }

        if (!user.isEnabled()) {
            throw new RuntimeException("Account is disabled");
        }

        // Check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Invalid password for user: {}", request.getUsername());
            int attempts = user.getFailedAttempts() + 1;
            user.setFailedAttempts(attempts);
            int maxAttempts = bruteforceProperties.getMaxFailedAttempts();
            if (attempts >= maxAttempts) {
                LocalDateTime lockoutEnd = LocalDateTime.now()
                        .plusMinutes(bruteforceProperties.getLockoutDurationMinutes());
                user.setLockoutEnd(lockoutEnd);
                user.setAccountNonLocked(false);
                userRepository.save(user);
                securityAuditSender.sendAccountLocked(user.getId(), user.getUsername(), "BRUTEFORCE");
                throw new AccountTemporarilyLockedException("Account temporarily locked");
            }
            userRepository.save(user);
            throw new RuntimeException("Invalid credentials");
        }

        // Success: reset failed attempts and lock
        user.setFailedAttempts(0);
        user.setLockoutEnd(null);
        user.setAccountNonLocked(true);
        userRepository.save(user);

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        log.info("User {} logged in successfully", request.getUsername());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime())
                .username(user.getUsername())
                .roles(user.getRoles().stream()
                        .map(Enum::name)
                        .collect(Collectors.toSet()))
                .build();
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        log.debug("Refreshing token");

        // Valider le refresh token
        if (!jwtService.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        // Extraire le username et récupérer l'utilisateur
        String username = jwtService.extractUsername(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Générer un nouveau access token
        String newAccessToken = jwtService.generateToken(user);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime())
                .username(user.getUsername())
                .roles(user.getRoles().stream()
                        .map(Enum::name)
                        .collect(Collectors.toSet()))
                .build();
    }

    @Override
    public boolean validateToken(String token) {
        log.debug("Validating token");
        return jwtService.validateToken(token);
    }

    @Override
    public void anonymizeAccount(Long userId) {
        log.info("Anonymizing account for user id: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setUsername("anonymized-" + userId);
        user.setEmail("anonymized-" + userId + "@anonymized.local");
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setEnabled(false);
        user.setAccountNonLocked(false);
        userRepository.save(user);
        log.info("Account anonymized for user id: {}", userId);
    }

    @Override
    public void deleteAccount(Long userId) {
        log.info("Deleting account for user id: {}", userId);
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(userId);
        log.info("Account deleted for user id: {}", userId);
    }
}
