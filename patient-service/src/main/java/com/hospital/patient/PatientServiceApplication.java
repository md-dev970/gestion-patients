package com.hospital.patient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                      PATIENT SERVICE APPLICATION                             ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS CLASS EXISTS:                                                      ║
 * ║  Entry point for the Patient microservice.                                   ║
 * ║                                                                              ║
 * ║  @EnableDiscoveryClient: Registers this service with Eureka                  ║
 * ║  @EnableFeignClients: Allows calling other microservices easily              ║
 * ║                                                                              ║
 * ║  Students: This service manages patient data.                                ║
 * ║  Port: 8081 (see application.yml)                                            ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class PatientServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PatientServiceApplication.class, args);
    }
}

