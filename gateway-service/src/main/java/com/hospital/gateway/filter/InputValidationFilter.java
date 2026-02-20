package com.hospital.gateway.filter;

import com.hospital.gateway.audit.SecurityAuditSender;
import com.hospital.gateway.config.InputValidationProperties;
import com.hospital.gateway.validation.InjectionPatterns;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Validation middleware (T1.7): enforces injection detection schema on query and headers.
 * Rejects invalid requests with 400 (US1.6). Runs after RateLimitFilter (order -80).
 * Emits SUSPICIOUS_INPUT when a pattern matches.
 */
@Component
public class InputValidationFilter implements GlobalFilter, Ordered {

    private static final String EVENT_TYPE = "SUSPICIOUS_INPUT";
    private static final String SOURCE_QUERY = "query";
    private static final String SOURCE_HEADER = "header";
    private static final String BAD_REQUEST_BODY = "{\"error\":\"Invalid or suspicious input\"}";
    private static final String HEADER_AUTHORIZATION = "Authorization";

    private final InputValidationProperties properties;
    private final InjectionPatterns injectionPatterns;
    private final SecurityAuditSender securityAuditSender;

    public InputValidationFilter(InputValidationProperties properties,
                                 InjectionPatterns injectionPatterns,
                                 SecurityAuditSender securityAuditSender) {
        this.properties = properties;
        this.injectionPatterns = injectionPatterns;
        this.securityAuditSender = securityAuditSender;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!properties.isEnabled()) {
            return chain.filter(exchange);
        }

        String path = exchange.getRequest().getPath().value();
        if (isExcludedPath(path)) {
            return chain.filter(exchange);
        }

        ServerHttpRequest request = exchange.getRequest();
        String method = request.getMethod() != null ? request.getMethod().name() : "";

        // Check query parameter values
        for (List<String> values : request.getQueryParams().values()) {
            for (String value : values) {
                if (value != null && !value.isBlank()) {
                    String category = injectionPatterns.match(value);
                    if (category != null) {
                        securityAuditSender.sendSuspiciousInput(EVENT_TYPE, SOURCE_QUERY, path, method, category);
                        return respondBadRequest(exchange);
                    }
                }
            }
        }

        // Check header values (exclude Authorization to avoid false positives on JWT)
        for (String headerName : request.getHeaders().keySet()) {
            if (HEADER_AUTHORIZATION.equalsIgnoreCase(headerName)) {
                continue;
            }
            List<String> values = request.getHeaders().get(headerName);
            if (values != null) {
                for (String value : values) {
                    if (value != null && !value.isBlank()) {
                        String category = injectionPatterns.match(value);
                        if (category != null) {
                            securityAuditSender.sendSuspiciousInput(EVENT_TYPE, SOURCE_HEADER, path, method, category);
                            return respondBadRequest(exchange);
                        }
                    }
                }
            }
        }

        return chain.filter(exchange);
    }

    private boolean isExcludedPath(String path) {
        List<String> excluded = properties.getExcludedPaths();
        if (excluded == null) return false;
        for (String prefix : excluded) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private Mono<Void> respondBadRequest(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        DataBuffer buffer = exchange.getResponse().bufferFactory()
                .wrap(BAD_REQUEST_BODY.getBytes(StandardCharsets.UTF_8));
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -80;
    }
}
