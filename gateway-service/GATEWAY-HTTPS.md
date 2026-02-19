# Security Gateway – HTTPS & Routing

This document explains how the `gateway-service` is positioned as the **single entry point**
for the KIT COMMUN microservices and how HTTPS support and tests are set up.

---

## 1. Role of `gateway-service`

- Technology: **Spring Cloud Gateway** (reactive, with Eureka discovery).
- Default listening port: **8080**.
- Responsibility:
  - Receive all external HTTP(S) traffic.
  - Route `/api/...` requests to the appropriate KIT COMMUN microservice.
  - Apply authentication and (later) authorization filters.

The routing configuration is defined in:

- `gateway-service/src/main/resources/application.yml`  
  using `spring.cloud.gateway.routes` with `lb://` URIs.

---

## 2. All external traffic goes through the gateway

In `docker-compose.yml`:

- `gateway-service` exposes its port to the host:

```yaml
gateway-service:
  ports:
    - "8080:8080"
```

- The KIT COMMUN microservices (`auth-service`, `patient-service`, `staff-service`,
  `appointment-service`, `medical-record-service`, `consultations-service`) **no longer
  expose** their ports to the host. They are only reachable inside the `hospital-network`
  bridge network.

Result:

- From outside Docker, clients can only reach the system at:  
  `http://localhost:8080/...` (and, when configured, via HTTPS).

---

## 3. HTTPS / TLS support

HTTPS support is enabled at the Spring Boot level via the `server.ssl` configuration
in `application.yml`:

```yaml
server:
  port: 8080
  ssl:
    enabled: ${SERVER_SSL_ENABLED:false}
    key-store: ${SERVER_SSL_KEY_STORE:}
    key-store-password: ${SERVER_SSL_KEY_STORE_PASSWORD:}
    key-store-type: ${SERVER_SSL_KEY_STORE_TYPE:PKCS12}
    key-alias: ${SERVER_SSL_KEY_ALIAS:gateway}
```

- By default, `SERVER_SSL_ENABLED` is `false` → the gateway runs in HTTP on port 8080.
- To enable HTTPS you must:
  1. Provide a valid key store (PKCS12/JKS) accessible from the container or JVM.
  2. Set environment variables, for example:

```bash
export SERVER_SSL_ENABLED=true
export SERVER_SSL_KEY_STORE=/path/to/gateway-keystore.p12
export SERVER_SSL_KEY_STORE_PASSWORD=changeit
export SERVER_SSL_KEY_STORE_TYPE=PKCS12
export SERVER_SSL_KEY_ALIAS=gateway
```

In Docker/Kubernetes, these values should be injected via environment variables and
the key store mounted as a volume or provided via a secret.

> Note: The project does **not** ship a key store file. It is the operator’s
> responsibility to generate and mount the certificate/key.

---

## 4. Routing configuration

The main routes are defined in `application.yml`:

- `/api/auth/**` → `lb://auth-service`
- `/api/patients/**` → `lb://patient-service`
- `/api/staff/**` → `lb://staff-service`
- `/api/appointments/**` → `lb://appointment-service`
- `/api/medical-records/**` → `lb://medical-record-service`
- `/api/consultations/**` → `lb://consultations-service`

This ensures that the public API surface of the KIT COMMUN is exposed consistently
through the gateway without changing the downstream endpoints.

---

## 5. Tests

Two main test classes exist for the gateway:

1. `AuthenticationFilterTest`
   - Unit tests for `AuthenticationFilter`, verifying:
     - Public paths (`/api/auth/login`, `/api/auth/register`, `/api/auth/refresh`,
       `/actuator/health`) are allowed without authentication.
     - Placeholder behavior for private paths (to be reinforced in security subject).

2. `GatewayRoutesConfigTest`
   - Spring Boot test that starts the gateway context and autowires the `RouteLocator`.
   - Verifies that the gateway defines routes for **all** core KIT COMMUN services:
     `auth-service`, `patient-service`, `staff-service`,
     `appointment-service`, `medical-record-service`, `consultations-service`.

These tests ensure that:

- The gateway intercepts and filters all traffic.
- All API paths of the KIT COMMUN are reachable **through** the gateway configuration.

For full end-to-end verification in Docker:

1. Start the stack with `docker-compose up -d --build`.
2. Call an API through the gateway, e.g.:

```bash
curl http://localhost:8080/api/patients/1
```

3. Verify that there is **no direct access** to the microservices on ports 8081–8086 from
the host (connections should fail), confirming that all external traffic must go
through the gateway.

