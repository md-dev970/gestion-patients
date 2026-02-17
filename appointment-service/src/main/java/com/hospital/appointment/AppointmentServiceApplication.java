package com.hospital.appointment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                    APPOINTMENT SERVICE APPLICATION                           ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS CLASS EXISTS:                                                      ║
 * ║  Entry point for the Appointment microservice.                               ║
 * ║  Manages scheduling between patients and medical staff.                      ║
 * ║                                                                              ║
 * ║  @EnableFeignClients: Required to call other microservices                   ║
 * ║  (Patient-Service, Staff-Service)                                            ║
 * ║                                                                              ║
 * ║  Port: 8083 (see application.yml)                                            ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class AppointmentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppointmentServiceApplication.class, args);
    }
}

