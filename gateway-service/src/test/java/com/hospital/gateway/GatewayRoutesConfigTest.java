package com.hospital.gateway;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;

import reactor.core.publisher.Flux;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that the API gateway is correctly configured to route
 * all KIT COMMUN APIs through the gateway service.
 */
@SpringBootTest
class GatewayRoutesConfigTest {

    @Autowired
    private RouteLocator routeLocator;

    @Test
    @DisplayName("Gateway has routes for all KIT COMMUN services")
    void gatewayHasRoutesForAllKitServices() {
        Flux<Route> routesFlux = routeLocator.getRoutes();
        List<String> routeIds = routesFlux
                .map(Route::getId)
                .collectList()
                .block();

        assertThat(routeIds)
                .as("Gateway must define routes for all core KIT COMMUN services")
                .contains(
                        "auth-service",
                        "patient-service",
                        "staff-service",
                        "appointment-service",
                        "medical-record-service",
                        "consultations-service"
                );
    }
}

