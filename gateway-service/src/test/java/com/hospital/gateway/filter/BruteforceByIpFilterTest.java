package com.hospital.gateway.filter;

import com.hospital.gateway.audit.SecurityAuditSender;
import com.hospital.gateway.bruteforce.BruteforceByIpStore;
import com.hospital.gateway.config.BruteforceByIpProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BruteforceByIpFilter Unit Tests")
class BruteforceByIpFilterTest {

    @Mock
    private GatewayFilterChain filterChain;

    private BruteforceByIpProperties properties;
    private BruteforceByIpStore bruteforceByIpStore;
    @Mock
    private SecurityAuditSender securityAuditSender;

    private BruteforceByIpFilter filter;

    @BeforeEach
    void setUp() {
        properties = new BruteforceByIpProperties();
        properties.setMaxFailedAttempts(3);
        properties.setLockoutDurationMinutes(15);
        properties.setLoginPath("/api/auth/login");
        bruteforceByIpStore = new BruteforceByIpStore(properties);
        filter = new BruteforceByIpFilter(properties, bruteforceByIpStore, securityAuditSender);
    }

    @Test
    @DisplayName("non-login path - chain called")
    void filter_nonLoginPath_callsChain() {
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/auth/register").build());
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, filterChain)).verifyComplete();

        verify(filterChain).filter(exchange);
    }

    @Test
    @DisplayName("GET login path - chain called")
    void filter_getLoginPath_callsChain() {
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/auth/login").build());
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, filterChain)).verifyComplete();

        verify(filterChain).filter(exchange);
    }

    @Test
    @DisplayName("POST login - not blocked - chain called")
    void filter_postLogin_notBlocked_callsChain() {
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/auth/login").build());
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, filterChain)).verifyComplete();

        verify(filterChain).filter(exchange);
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    @DisplayName("POST login - downstream returns 401 - recordFailure and audit on Nth")
    void filter_postLogin_401_recordFailure_andAuditOnNth() {
        when(filterChain.filter(any(ServerWebExchange.class))).thenAnswer(inv -> {
            ServerWebExchange ex = inv.getArgument(0);
            ex.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return Mono.empty();
        });

        for (int i = 0; i < 3; i++) {
            ServerWebExchange exchange = MockServerWebExchange.from(
                    MockServerHttpRequest.post("/api/auth/login").build());
            filter.filter(exchange, filterChain).block();
        }

        verify(securityAuditSender, times(1)).sendRateLimitExceeded(
                eq("BRUTEFORCE_IP"), any(), eq(3L), eq(900L));
    }

    @Test
    @DisplayName("POST login - IP blocked - 423 without calling chain")
    void filter_postLogin_blocked_returns423_noChain() {
        bruteforceByIpStore.recordFailure("192.168.1.1");
        bruteforceByIpStore.recordFailure("192.168.1.1");
        bruteforceByIpStore.recordFailure("192.168.1.1");

        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/auth/login")
                        .header("X-Forwarded-For", "192.168.1.1")
                        .build());

        StepVerifier.create(filter.filter(exchange, filterChain)).verifyComplete();

        verify(filterChain, never()).filter(any());
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.LOCKED);
        assertThat(exchange.getResponse().getHeaders().getContentType().toString()).contains("application/json");
    }
}
