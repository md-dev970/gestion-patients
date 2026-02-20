package com.hospital.gateway.filter;

import com.hospital.gateway.audit.SecurityAuditSender;
import com.hospital.gateway.config.RateLimitProperties;
import com.hospital.gateway.ratelimit.RateLimitStore;
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
 * Global filter that enforces rate limits by IP and by authenticated user.
 * Runs after AuthenticationFilter (order -90). Returns 429 and emits RATE_LIMIT_EXCEEDED when exceeded.
 */
@Component
public class RateLimitFilter implements GlobalFilter, Ordered {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String KEY_PREFIX_IP = "ip:";
    private static final String KEY_PREFIX_USER = "user:";
    private static final String KEY_TYPE_IP = "IP";
    private static final String KEY_TYPE_USER = "USER";
    private static final String TOO_MANY_REQUESTS_BODY = "{\"error\":\"Too Many Requests\"}";

    private final RateLimitProperties properties;
    private final RateLimitStore rateLimitStore;
    private final SecurityAuditSender securityAuditSender;

    public RateLimitFilter(RateLimitProperties properties, RateLimitStore rateLimitStore,
                           SecurityAuditSender securityAuditSender) {
        this.properties = properties;
        this.rateLimitStore = rateLimitStore;
        this.securityAuditSender = securityAuditSender;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        if (isExcludedPath(path)) {
            return chain.filter(exchange);
        }

        String clientIp = resolveClientIp(exchange.getRequest());
        String ipKey = KEY_PREFIX_IP + clientIp;

        if (!rateLimitStore.tryConsume(ipKey, properties.getRequestsPerMinutePerIp())) {
            return respondTooManyRequests(exchange, KEY_TYPE_IP, clientIp,
                    properties.getRequestsPerMinutePerIp(), properties.getWindowSeconds());
        }

        String userId = exchange.getRequest().getHeaders().getFirst(HEADER_USER_ID);
        if (userId != null && !userId.isBlank()) {
            String userKey = KEY_PREFIX_USER + userId;
            if (!rateLimitStore.tryConsume(userKey, properties.getRequestsPerMinutePerUser())) {
                return respondTooManyRequests(exchange, KEY_TYPE_USER, userId,
                        properties.getRequestsPerMinutePerUser(), properties.getWindowSeconds());
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

    private String resolveClientIp(ServerHttpRequest request) {
        String forwarded = request.getHeaders().getFirst(HEADER_X_FORWARDED_FOR);
        if (forwarded != null && !forwarded.isBlank()) {
            int comma = forwarded.indexOf(',');
            String first = comma >= 0 ? forwarded.substring(0, comma).trim() : forwarded.trim();
            if (!first.isEmpty()) return first;
        }
        if (request.getRemoteAddress() != null && request.getRemoteAddress().getAddress() != null) {
            return request.getRemoteAddress().getAddress().getHostAddress();
        }
        return "unknown";
    }

    private Mono<Void> respondTooManyRequests(ServerWebExchange exchange, String keyType, String key,
                                               long limit, long windowSeconds) {
        securityAuditSender.sendRateLimitExceeded(keyType, key, limit, windowSeconds);
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        DataBuffer buffer = exchange.getResponse().bufferFactory()
                .wrap(TOO_MANY_REQUESTS_BODY.getBytes(StandardCharsets.UTF_8));
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -90;
    }
}
