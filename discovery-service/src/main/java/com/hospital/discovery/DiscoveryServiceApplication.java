package com.hospital.discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                      EUREKA DISCOVERY SERVICE                                ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS CLASS EXISTS:                                                      ║
 * ║  This is the entry point for the Eureka Server.                              ║
 * ║                                                                              ║
 * ║  @EnableEurekaServer: Transforms this Spring Boot app into a service         ║
 * ║  registry. Other microservices will register themselves here.                ║
 * ║                                                                              ║
 * ║  Students: Start this service FIRST before any other microservice.           ║
 * ║  Access the dashboard at: http://localhost:8761                              ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
@SpringBootApplication
@EnableEurekaServer
public class DiscoveryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DiscoveryServiceApplication.class, args);
    }
}

