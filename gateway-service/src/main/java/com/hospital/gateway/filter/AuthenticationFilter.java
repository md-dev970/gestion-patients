package com.hospital.gateway.filter;

import com.hospital.gateway.service.JwtClaims;
import com.hospital.gateway.service.JwtVerificationService;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Global filter that validates JWT on every request (except public paths).
 * Returns 401 for missing, invalid or expired tokens; forwards claims via
 * X-User-Id, X-Username, X-User-Roles when valid.
 */
@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtVerificationService jwtVerificationService;

    public AuthenticationFilter(JwtVerificationService jwtVerificationService) {
        this.jwtVerificationService = jwtVerificationService;
    }

    /**
     * Paths that do NOT require authentication.
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

        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return respondUnauthorized(exchange, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(BEARER_PREFIX.length()).trim();
        Optional<JwtClaims> claimsOpt = jwtVerificationService.verifyAndGetClaims(token);

        if (claimsOpt.isEmpty()) {
            return respondUnauthorized(exchange, "Invalid or expired token");
        }

        JwtClaims claims = claimsOpt.get();
        ServerWebExchange mutated = exchange.mutate()
                .request(builder -> {
                    if (claims.userId() != null) {
                        builder.header("X-User-Id", String.valueOf(claims.userId()));
                    }
                    builder.header("X-Username", claims.username());
                    builder.header("X-User-Roles", String.join(",", claims.roles()));
                })
                .build();

        return chain.filter(mutated);
    }

    private Mono<Void> respondUnauthorized(ServerWebExchange exchange, String errorMessage) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        exchange.getResponse().getHeaders().add(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        String body = "{\"error\":\"" + escapeJson(errorMessage) + "\"}";
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private boolean isPublicPath(String path) {
        for (String publicPath : PUBLIC_PATHS) {
            if (path.startsWith(publicPath)) {
                return true;
            }
        }
        return false;
    }
}
