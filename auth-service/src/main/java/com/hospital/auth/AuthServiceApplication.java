package com.hospital.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                    AUTHENTICATION SERVICE APPLICATION                        ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS CLASS EXISTS:                                                      ║
 * ║  Entry point for the Authentication microservice.                            ║
 * ║  Handles user authentication and JWT token management.                       ║
 * ║                                                                              ║
 * ║  // Security will be reinforced in Subject 3                                 ║
 * ║                                                                              ║
 * ║  Students: This is a SKELETON service.                                       ║
 * ║  Full security implementation is required in Subject 3.                      ║
 * ║  Port: 8085 (see application.yml)                                            ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
@SpringBootApplication
@EnableDiscoveryClient
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}

