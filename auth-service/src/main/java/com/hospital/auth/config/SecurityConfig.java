package com.hospital.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                      SECURITY CONFIGURATION                                  ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS CLASS EXISTS:                                                      ║
 * ║  Configures Spring Security for the auth service.                            ║
 * ║                                                                              ║
 * ║  // Security will be reinforced in Subject 3                                 ║
 * ║                                                                              ║
 * ║  IMPORTANT: This is a MINIMAL configuration.                                 ║
 * ║  Students MUST implement proper security in Subject 3:                       ║
 * ║    - JWT filter                                                              ║
 * ║    - Proper endpoint protection                                              ║
 * ║    - CORS configuration                                                      ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            .authorizeHttpRequests(auth -> auth
                // 1. SWAGGER UI WHITELIST (Added this block)
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()

                // 2. Public Auth Endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                
                // 3. All other requests need authentication
                .anyRequest().authenticated()
            );

                    // TODO: Add JWT authentication filter in Subject 3
        // http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
