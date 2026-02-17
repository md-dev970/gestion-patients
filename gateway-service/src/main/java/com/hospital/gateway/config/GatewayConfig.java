package com.hospital.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                       GATEWAY ROUTING CONFIGURATION                          ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS CLASS EXISTS:                                                      ║
 * ║  Defines HOW requests are routed to each microservice.                       ║
 * ║                                                                              ║
 * ║  Routing can be configured in TWO ways:                                      ║
 * ║    1. Java Config (this file) - More flexible, IDE support                   ║
 * ║    2. YAML Config (application.yml) - Easier to modify without recompiling   ║
 * ║                                                                              ║
 * ║  Students: This file shows programmatic routing. See application.yml         ║
 * ║  for the YAML-based approach used in production.                             ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
@Configuration
public class GatewayConfig {

    /**
     * Defines custom routes programmatically.
     * 
     * WHY: This is an ALTERNATIVE to YAML configuration.
     * Students: We use YAML in application.yml, so this is commented out.
     * Uncomment if you prefer Java-based routing configuration.
     */
    // @Bean
    // public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
    //     return builder.routes()
    //         // Route to Patient Service
    //         .route("patient-service", r -> r
    //             .path("/api/patients/**")
    //             .uri("lb://patient-service"))  // lb:// means load-balanced via Eureka
    //         
    //         // Route to Staff Service
    //         .route("staff-service", r -> r
    //             .path("/api/staff/**")
    //             .uri("lb://staff-service"))
    //         
    //         // Route to Appointment Service
    //         .route("appointment-service", r -> r
    //             .path("/api/appointments/**")
    //             .uri("lb://appointment-service"))
    //         
    //         // Route to Medical Record Service
    //         .route("medical-record-service", r -> r
    //             .path("/api/medical-records/**")
    //             .uri("lb://medical-record-service"))
    //         
    //         // Route to Auth Service
    //         .route("auth-service", r -> r
    //             .path("/api/auth/**")
    //             .uri("lb://auth-service"))
    //         
    //         .build();
    // }
}

