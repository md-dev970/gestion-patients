package com.hospital.gateway.audit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("HttpSecurityAuditSender sendSuspiciousInput Unit Tests")
class HttpSecurityAuditSenderTest {

    private MockWebServer server;
    private ObjectMapper objectMapper;
    private HttpSecurityAuditSender sender;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.enqueue(new MockResponse().setResponseCode(200));
        server.start();
        objectMapper = new ObjectMapper();
        String auditUrl = server.url("/api/events").toString();
        sender = new HttpSecurityAuditSender(WebClient.builder(), auditUrl);
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    @DisplayName("sendSuspiciousInput POSTs JSON with eventType, category, source, path, method, timestamp")
    void sendSuspiciousInput_postsCorrectPayload() throws Exception {
        sender.sendSuspiciousInput("SUSPICIOUS_INPUT", "query", "/api/patients/search", "GET", "SQLI");

        RecordedRequest request = server.takeRequest(2, TimeUnit.SECONDS);
        assertThat(request).isNotNull();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/api/events");

        JsonNode body = objectMapper.readTree(request.getBody().readUtf8());
        assertThat(body.get("eventType").asText()).isEqualTo("SUSPICIOUS_INPUT");
        assertThat(body.get("category").asText()).isEqualTo("SQLI");
        assertThat(body.get("source").asText()).isEqualTo("query");
        assertThat(body.get("path").asText()).isEqualTo("/api/patients/search");
        assertThat(body.get("method").asText()).isEqualTo("GET");
        assertThat(body.has("timestamp")).isTrue();
    }
}
