package com.hospital.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                           API GATEWAY APPLICATION                            ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS CLASS EXISTS:                                                      ║
 * ║  Entry point for the API Gateway service.                                    ║
 * ║                                                                              ║
 * ║  @EnableDiscoveryClient: Allows this gateway to discover other services      ║
 * ║  registered in Eureka and route requests to them.                            ║
 * ║                                                                              ║
 * ║  Students: The Gateway is the ONLY service exposed to external clients.      ║
 * ║  Access at: http://localhost:8080                                            ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
@SpringBootApplication
@EnableDiscoveryClient
public class GatewayServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
    }
}

