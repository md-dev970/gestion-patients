package com.hospital.gateway.filter;

import com.hospital.gateway.audit.SecurityAuditSender;
import com.hospital.gateway.bruteforce.BruteforceByIpStore;
import com.hospital.gateway.config.BruteforceByIpProperties;
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

/**
 * Anti-bruteforce by IP (T1.6): counter per IP, TTL block after N failed logins (401/423).
 * Only applies to POST on the configured login path. When blocked, returns 423 without proxying.
 */
@Component
public class BruteforceByIpFilter implements GlobalFilter, Ordered {

    private static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String KEY_TYPE_BRUTEFORCE_IP = "BRUTEFORCE_IP";
    private static final String BODY_LOCKED = "{\"error\":\"Too many login attempts. Try again later.\"}";

    private final BruteforceByIpProperties properties;
    private final BruteforceByIpStore bruteforceByIpStore;
    private final SecurityAuditSender securityAuditSender;

    public BruteforceByIpFilter(BruteforceByIpProperties properties, BruteforceByIpStore bruteforceByIpStore,
                               SecurityAuditSender securityAuditSender) {
        this.properties = properties;
        this.bruteforceByIpStore = bruteforceByIpStore;
        this.securityAuditSender = securityAuditSender;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod() != null ? exchange.getRequest().getMethod().name() : "";

        if (!"POST".equalsIgnoreCase(method) || !path.equals(properties.getLoginPath())) {
            return chain.filter(exchange);
        }

        String clientIp = resolveClientIp(exchange.getRequest());
        if (bruteforceByIpStore.isBlocked(clientIp)) {
            return respondLocked(exchange);
        }

        return chain.filter(exchange)
                .doOnSuccess(v -> {
                    var statusCode = exchange.getResponse().getStatusCode();
                    if (statusCode != null && (statusCode.value() == 401 || statusCode.value() == 403 || statusCode.value() == 423)) {
                        boolean justBlocked = bruteforceByIpStore.recordFailure(clientIp);
                        if (justBlocked) {
                            long windowSeconds = properties.getLockoutDurationMinutes() * 60L;
                            securityAuditSender.sendRateLimitExceeded(
                                    KEY_TYPE_BRUTEFORCE_IP, clientIp,
                                    properties.getMaxFailedAttempts(), windowSeconds);
                        }
                    }
                });
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

    private Mono<Void> respondLocked(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.LOCKED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        DataBuffer buffer = exchange.getResponse().bufferFactory()
                .wrap(BODY_LOCKED.getBytes(StandardCharsets.UTF_8));
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -92;
    }
}
