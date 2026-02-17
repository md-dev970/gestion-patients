package com.hospital.staff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                       STAFF SERVICE APPLICATION                              ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS CLASS EXISTS:                                                      ║
 * ║  Entry point for the Staff microservice.                                     ║
 * ║  Manages doctors, nurses, and other hospital personnel.                      ║
 * ║                                                                              ║
 * ║  Students: This service manages medical staff data.                          ║
 * ║  Port: 8082 (see application.yml)                                            ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class StaffServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(StaffServiceApplication.class, args);
    }
}

