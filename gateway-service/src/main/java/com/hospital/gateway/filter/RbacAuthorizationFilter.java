package com.hospital.gateway.filter;

import com.hospital.gateway.audit.SecurityAuditSender;
import com.hospital.gateway.rbac.Action;
import com.hospital.gateway.rbac.Resource;
import com.hospital.gateway.service.RbacService;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Global filter that enforces RBAC on patient-dossier paths after authentication.
 * Runs after AuthenticationFilter (order -50). Returns 403 and emits ACCESS_DENIED when not allowed.
 */
@Component
public class RbacAuthorizationFilter implements GlobalFilter, Ordered {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USERNAME = "X-Username";
    private static final String HEADER_USER_ROLES = "X-User-Roles";
    private static final String FORBIDDEN_BODY = "{\"error\":\"Forbidden\"}";

    private final RbacService rbacService;
    private final SecurityAuditSender securityAuditSender;

    public RbacAuthorizationFilter(RbacService rbacService, SecurityAuditSender securityAuditSender) {
        this.rbacService = rbacService;
        this.securityAuditSender = securityAuditSender;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        Optional<Resource> resourceOpt = rbacService.resolveResource(path);

        if (resourceOpt.isEmpty()) {
            return chain.filter(exchange);
        }

        String username = exchange.getRequest().getHeaders().getFirst(HEADER_USERNAME);
        if (username == null || username.isBlank()) {
            return chain.filter(exchange);
        }

        String rolesHeader = exchange.getRequest().getHeaders().getFirst(HEADER_USER_ROLES);
        List<String> roles = parseRoles(rolesHeader);
        String method = exchange.getRequest().getMethod().name();
        String userId = exchange.getRequest().getHeaders().getFirst(HEADER_USER_ID);

        if (rbacService.isAllowed(path, method, roles, userId)) {
            return chain.filter(exchange);
        }

        String deniedUserId = exchange.getRequest().getHeaders().getFirst(HEADER_USER_ID);
        Resource resource = resourceOpt.get();
        Action action = rbacService.resolveAction(method);
        String resourceId = rbacService.extractResourceId(path, resource);

        securityAuditSender.sendAccessDenied(
                deniedUserId != null ? deniedUserId : "",
                resource,
                resourceId != null ? resourceId : "",
                action,
                "RBAC_DENY"
        );

        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        exchange.getResponse().getHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(FORBIDDEN_BODY.getBytes(StandardCharsets.UTF_8));
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    private static List<String> parseRoles(String rolesHeader) {
        if (rolesHeader == null || rolesHeader.isBlank()) {
            return List.of();
        }
        return Arrays.stream(rolesHeader.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    @Override
    public int getOrder() {
        return -50;
    }
}
