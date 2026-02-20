package com.hospital.gateway.filter;

import com.hospital.gateway.audit.SecurityAuditSender;
import com.hospital.gateway.config.RateLimitProperties;
import com.hospital.gateway.ratelimit.RateLimitStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RateLimitFilter Unit Tests")
class RateLimitFilterTest {

    @Mock
    private GatewayFilterChain filterChain;

    private RateLimitProperties properties;
    private RateLimitStore rateLimitStore;
    @Mock
    private SecurityAuditSender securityAuditSender;

    private RateLimitFilter filter;

    @BeforeEach
    void setUp() {
        properties = new RateLimitProperties();
        properties.setRequestsPerMinutePerIp(2);
        properties.setRequestsPerMinutePerUser(2);
        properties.setWindowSeconds(60);
        properties.setExcludedPaths(List.of("/actuator/health"));
        rateLimitStore = new RateLimitStore(properties);
        filter = new RateLimitFilter(properties, rateLimitStore, securityAuditSender);
    }

    @Test
    @DisplayName("excluded path - chain called without consuming quota")
    void filter_excludedPath_callsChain() {
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/actuator/health").build());
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, filterChain)).verifyComplete();

        verify(filterChain).filter(exchange);
        verify(securityAuditSender, never()).sendRateLimitExceeded(any(), any(), anyLong(), anyLong());
    }

    @Test
    @DisplayName("request under IP limit - chain called")
    void filter_underIpLimit_callsChain() {
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/patients/1").build());
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, filterChain)).verifyComplete();

        verify(filterChain).filter(exchange);
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    @DisplayName("request over IP limit - 429 and audit invoked")
    void filter_overIpLimit_returns429_andAuditInvoked() {
        // Exhaust IP quota (same remote address for MockServerHttpRequest)
        ServerWebExchange e1 = MockServerWebExchange.from(MockServerHttpRequest.get("/api/auth/login").build());
        ServerWebExchange e2 = MockServerWebExchange.from(MockServerHttpRequest.get("/api/auth/login").build());
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());
        filter.filter(e1, filterChain).block();
        filter.filter(e2, filterChain).block();

        ServerWebExchange e3 = MockServerWebExchange.from(MockServerHttpRequest.get("/api/auth/login").build());
        StepVerifier.create(filter.filter(e3, filterChain)).verifyComplete();

        verify(filterChain, times(2)).filter(any());
        assertThat(e3.getResponse().getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(e3.getResponse().getHeaders().getContentType().toString()).contains("application/json");

        ArgumentCaptor<String> keyTypeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(securityAuditSender).sendRateLimitExceeded(
                keyTypeCaptor.capture(),
                keyCaptor.capture(),
                eq(2L),
                eq(60L));
        assertThat(keyTypeCaptor.getValue()).isEqualTo("IP");
        assertThat(keyCaptor.getValue()).isNotEmpty();
    }

    @Test
    @DisplayName("request with X-User-Id over user limit - 429 and USER audit")
    void filter_overUserLimit_returns429_andUserAudit() {
        // Use a unique IP per request so we don't hit IP limit (mock different remotes)
        properties.setRequestsPerMinutePerIp(1000);
        rateLimitStore = new RateLimitStore(properties);
        filter = new RateLimitFilter(properties, rateLimitStore, securityAuditSender);

        String path = "/api/patients/1";
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // First two requests with same user allowed
        ServerWebExchange r1 = exchangeWithUser(path, "10");
        ServerWebExchange r2 = exchangeWithUser(path, "10");
        filter.filter(r1, filterChain).block();
        filter.filter(r2, filterChain).block();

        // Third request with same user -> 429
        ServerWebExchange r3 = exchangeWithUser(path, "10");
        StepVerifier.create(filter.filter(r3, filterChain)).verifyComplete();

        assertThat(r3.getResponse().getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        ArgumentCaptor<String> keyTypeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(securityAuditSender).sendRateLimitExceeded(
                keyTypeCaptor.capture(),
                keyCaptor.capture(),
                eq(2L),
                eq(60L));
        assertThat(keyTypeCaptor.getValue()).isEqualTo("USER");
        assertThat(keyCaptor.getValue()).isEqualTo("10");
    }

    @Test
    @DisplayName("getOrder returns -90")
    void getOrder_returns90() {
        assertThat(filter.getOrder()).isEqualTo(-90);
    }

    private static ServerWebExchange exchangeWithUser(String path, String userId) {
        MockServerHttpRequest request = MockServerHttpRequest.get(path)
                .header("X-User-Id", userId)
                .build();
        return MockServerWebExchange.from(request);
    }
}
