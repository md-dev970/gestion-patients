package com.hospital.consultation;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Application principale du microservice Consultations.
 * Conforme au Kit Commun.
 * 
 * Fonctionnalités:
 *   - Création d'une consultation
 *   - Consultation de l'historique d'un patient
 *   - Mise à jour d'une consultation
 */
@SpringBootApplication
@EnableDiscoveryClient
@OpenAPIDefinition(
        info = @Info(
                title = "Consultations Service API",
                version = "1.0",
                description = "API de gestion des consultations médicales - Kit Commun",
                contact = @Contact(name = "EHTP", url = "https://www.ehtp.ac.ma")
        )
)
public class ConsultationsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsultationsServiceApplication.class, args);
    }
}
