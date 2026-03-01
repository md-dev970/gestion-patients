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
import com.hospital.auth.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Unit Tests")
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private BruteforceProperties bruteforceProperties;

    @Mock
    private SecurityAuditSender securityAuditSender;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;
    private String hashedPassword;
    private String accessToken;
    private String refreshToken;

    @BeforeEach
    void setUp() {
        hashedPassword = "$2a$10$hashedPassword";
        accessToken = "access-token";
        refreshToken = "refresh-token";

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setRole("ROLE_PATIENT");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password(hashedPassword)
                .roles(Set.of(Role.ROLE_PATIENT))
                .enabled(true)
                .accountNonLocked(true)
                .failedAttempts(0)
                .lockoutEnd(null)
                .build();
    }

    @Test
    @DisplayName("register - valid request - returns AuthResponse")
    void register_validRequest_returnsAuthResponse() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn(hashedPassword);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn(accessToken);
        when(jwtService.generateRefreshToken(user)).thenReturn(refreshToken);
        when(jwtService.getExpirationTime()).thenReturn(3600L);

        // When
        AuthResponse result = authService.register(registerRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo(accessToken);
        assertThat(result.getRefreshToken()).isEqualTo(refreshToken);
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getTokenType()).isEqualTo("Bearer");
        assertThat(result.getRoles()).contains("ROLE_PATIENT");
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(user);
        verify(jwtService).generateRefreshToken(user);
    }

    @Test
    @DisplayName("register - duplicate username - throws RuntimeException")
    void register_duplicateUsername_throwsRuntimeException() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Username already exists");
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository, never()).existsByEmail(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("register - duplicate email - throws RuntimeException")
    void register_duplicateEmail_throwsRuntimeException() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email already exists");
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("register - invalid role - defaults to ROLE_PATIENT")
    void register_invalidRole_defaultsToROLE_PATIENT() {
        // Given
        registerRequest.setRole("INVALID_ROLE");
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn(hashedPassword);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn(accessToken);
        when(jwtService.generateRefreshToken(user)).thenReturn(refreshToken);
        when(jwtService.getExpirationTime()).thenReturn(3600L);

        // When
        AuthResponse result = authService.register(registerRequest);

        // Then
        assertThat(result).isNotNull();
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("login - valid credentials - returns AuthResponse")
    void login_validCredentials_returnsAuthResponse() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", hashedPassword)).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn(accessToken);
        when(jwtService.generateRefreshToken(user)).thenReturn(refreshToken);
        when(jwtService.getExpirationTime()).thenReturn(3600L);

        // When
        AuthResponse result = authService.login(loginRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo(accessToken);
        assertThat(result.getRefreshToken()).isEqualTo(refreshToken);
        assertThat(result.getUsername()).isEqualTo("testuser");
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("password123", hashedPassword);
        verify(jwtService).generateToken(user);
        verify(jwtService).generateRefreshToken(user);
    }

    @Test
    @DisplayName("login - user not found - throws RuntimeException")
    void login_userNotFound_throwsRuntimeException() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("login - invalid password - throws RuntimeException")
    void login_invalidPassword_throwsRuntimeException() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", hashedPassword)).thenReturn(false);
        when(bruteforceProperties.getMaxFailedAttempts()).thenReturn(5);

        // When/Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid credentials");
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("password123", hashedPassword);
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("login - N failed attempts - locks account and throws AccountTemporarilyLockedException and sends audit")
    void login_nFailedAttempts_locksAccountAndSendsAudit() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", hashedPassword)).thenReturn(false);
        when(bruteforceProperties.getMaxFailedAttempts()).thenReturn(3);
        when(bruteforceProperties.getLockoutDurationMinutes()).thenReturn(15);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // 1st and 2nd failure: Invalid credentials
        for (int i = 0; i < 2; i++) {
            user.setFailedAttempts(i);
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Invalid credentials");
        }
        verify(securityAuditSender, never()).sendAccountLocked(anyLong(), any(), any());

        // 3rd failure: lock and AccountTemporarilyLockedException
        user.setFailedAttempts(2);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(AccountTemporarilyLockedException.class)
                .hasMessageContaining("Account temporarily locked");
        verify(securityAuditSender).sendAccountLocked(eq(1L), eq("testuser"), eq("BRUTEFORCE"));
        verify(userRepository, atLeast(1)).save(argThat(u -> u.getFailedAttempts() == 3 && !u.isAccountNonLocked() && u.getLockoutEnd() != null));
    }

    @Test
    @DisplayName("login - success after failures - resets failedAttempts and lockoutEnd")
    void login_successAfterFailures_resetsState() {
        user.setFailedAttempts(2);
        user.setLockoutEnd(null);
        user.setAccountNonLocked(true);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", hashedPassword)).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn(accessToken);
        when(jwtService.generateRefreshToken(user)).thenReturn(refreshToken);
        when(jwtService.getExpirationTime()).thenReturn(3600L);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthResponse result = authService.login(loginRequest);

        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo(accessToken);
        verify(userRepository).save(argThat(u -> u.getFailedAttempts() == 0 && u.getLockoutEnd() == null && u.isAccountNonLocked()));
    }

    @Test
    @DisplayName("login - disabled account - throws RuntimeException")
    void login_disabledAccount_throwsRuntimeException() {
        // Given (enabled check happens after lock check, before password)
        user.setEnabled(false);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // When/Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Account is disabled");
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("login - locked account - throws AccountTemporarilyLockedException")
    void login_lockedAccount_throwsAccountTemporarilyLockedException() {
        // Given: account locked with lockoutEnd in the future (we throw before password check)
        user.setAccountNonLocked(false);
        user.setLockoutEnd(LocalDateTime.now().plusMinutes(5));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // When/Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(AccountTemporarilyLockedException.class)
                .hasMessageContaining("Account temporarily locked");
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("refreshToken - valid token - returns AuthResponse with new access token")
    void refreshToken_validToken_returnsAuthResponseWithNewAccessToken() {
        // Given
        String newAccessToken = "new-access-token";
        when(jwtService.validateToken(refreshToken)).thenReturn(true);
        when(jwtService.extractUsername(refreshToken)).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn(newAccessToken);
        when(jwtService.getExpirationTime()).thenReturn(3600L);

        // When
        AuthResponse result = authService.refreshToken(refreshToken);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo(newAccessToken);
        assertThat(result.getRefreshToken()).isEqualTo(refreshToken);
        assertThat(result.getUsername()).isEqualTo("testuser");
        verify(jwtService).validateToken(refreshToken);
        verify(jwtService).extractUsername(refreshToken);
        verify(userRepository).findByUsername("testuser");
        verify(jwtService).generateToken(user);
    }

    @Test
    @DisplayName("refreshToken - invalid token - throws RuntimeException")
    void refreshToken_invalidToken_throwsRuntimeException() {
        // Given
        when(jwtService.validateToken(refreshToken)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> authService.refreshToken(refreshToken))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid refresh token");
        verify(jwtService).validateToken(refreshToken);
        verify(jwtService, never()).extractUsername(any());
        verify(userRepository, never()).findByUsername(any());
    }

    @Test
    @DisplayName("refreshToken - user not found - throws RuntimeException")
    void refreshToken_userNotFound_throwsRuntimeException() {
        // Given
        when(jwtService.validateToken(refreshToken)).thenReturn(true);
        when(jwtService.extractUsername(refreshToken)).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> authService.refreshToken(refreshToken))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
        verify(jwtService).validateToken(refreshToken);
        verify(jwtService).extractUsername(refreshToken);
        verify(userRepository).findByUsername("testuser");
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("validateToken - valid token - returns true")
    void validateToken_validToken_returnsTrue() {
        // Given
        when(jwtService.validateToken(accessToken)).thenReturn(true);

        // When
        boolean result = authService.validateToken(accessToken);

        // Then
        assertThat(result).isTrue();
        verify(jwtService).validateToken(accessToken);
    }

    @Test
    @DisplayName("validateToken - invalid token - returns false")
    void validateToken_invalidToken_returnsFalse() {
        // Given
        when(jwtService.validateToken(accessToken)).thenReturn(false);

        // When
        boolean result = authService.validateToken(accessToken);

        // Then
        assertThat(result).isFalse();
        verify(jwtService).validateToken(accessToken);
    }

    @Test
    @DisplayName("anonymizeAccount - user found - anonymizes and disables")
    void anonymizeAccount_userFound_anonymizesAndDisables() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(passwordEncoder.encode(any())).thenReturn("random-hash");

        authService.anonymizeAccount(1L);

        verify(userRepository).save(argThat(u ->
                u.getUsername().startsWith("anonymized-") && u.getEmail().contains("@anonymized.local")
                        && !u.isEnabled() && !u.isAccountNonLocked()));
    }

    @Test
    @DisplayName("anonymizeAccount - user not found - throws")
    void anonymizeAccount_userNotFound_throws() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.anonymizeAccount(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("deleteAccount - user exists - deletes")
    void deleteAccount_userExists_deletes() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        authService.deleteAccount(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deleteAccount - user not found - throws")
    void deleteAccount_userNotFound_throws() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> authService.deleteAccount(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }
}





