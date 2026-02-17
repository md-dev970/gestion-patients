package com.hospital.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                    AUTHENTICATION FILTER (PLACEHOLDER)                       ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  WHY THIS CLASS EXISTS:                                                      ║
 * ║  This filter intercepts ALL requests passing through the gateway.            ║
 * ║  It validates JWT tokens and checks authentication.                          ║
 * ║                                                                              ║
 * ║  // Security will be reinforced in Subject 3                                 ║
 * ║  // Currently: placeholder implementation                                    ║
 * ║                                                                              ║
 * ║  Students: You MUST implement proper JWT validation in Subject 3.            ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    // TODO: Inject JwtUtil or AuthService client in Subject 3
    
    /**
     * List of paths that do NOT require authentication.
     * WHY: Login and register endpoints must be accessible without a token.
     */
    private static final String[] PUBLIC_PATHS = {
        "/api/auth/login",
        "/api/auth/register",
        "/api/auth/refresh",
        "/actuator/health"
    };

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        
        // Check if the path is public (no auth required)
        if (isPublicPath(path)) {
            // Business logic will be added in the specialized subject
            return chain.filter(exchange);
        }

        // ═══════════════════════════════════════════════════════════════════════
        // PLACEHOLDER: Token validation logic goes here
        // Security will be reinforced in Subject 3
        // ═══════════════════════════════════════════════════════════════════════
        
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // TODO: Properly handle unauthorized access in Subject 3
            // For now, we log and continue (INSECURE - for development only)
            System.out.println("[AUTH FILTER] No token provided for path: " + path);
            
            // Uncomment this in Subject 3 to enforce authentication:
            // exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            // return exchange.getResponse().setComplete();
        }

        // Permissions will be checked in Subject 2
        // TODO: Add role-based access control here
        
        return chain.filter(exchange);
    }

    /**
     * Determines the filter execution order.
     * WHY: Lower numbers = higher priority. Auth should run EARLY.
     */
    @Override
    public int getOrder() {
        return -100; // High priority - run before other filters
    }

    /**
     * Checks if a path is public (does not require authentication).
     * 
     * @param path The request path
     * @return true if the path is public
     */
    private boolean isPublicPath(String path) {
        for (String publicPath : PUBLIC_PATHS) {
            if (path.startsWith(publicPath)) {
                return true;
            }
        }
        return false;
    }
}

