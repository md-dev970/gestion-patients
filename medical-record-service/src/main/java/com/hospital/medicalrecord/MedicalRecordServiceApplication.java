package com.hospital.medicalrecord;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                   MEDICAL RECORD SERVICE APPLICATION                         ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS CLASS EXISTS:                                                      ║
 * ║  Entry point for the Medical Record microservice.                            ║
 * ║  Manages sensitive patient health data and medical history.                  ║
 * ║                                                                              ║
 * ║  SECURITY CRITICAL: This service handles PHI (Protected Health Information). ║
 * ║  // Security will be reinforced in Subject 3                                 ║
 * ║                                                                              ║
 * ║  Port: 8084 (see application.yml)                                            ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class MedicalRecordServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedicalRecordServiceApplication.class, args);
    }
}

