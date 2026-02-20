# Security Gateway – HTTPS & Routing

This document explains how the `gateway-service` is positioned as the **single entry point**
for the KIT COMMUN microservices and how HTTPS support and tests are set up.

---

## 1. Role of `gateway-service`

This service is the **security-gateway** (reverse proxy) for the KIT COMMUN stack (T1.1). All client traffic goes through it before reaching backend microservices.

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

**Verifying the reverse proxy is operational (T1.1)**  
After starting the stack (`docker-compose up -d --build`), wait for the gateway to be healthy. Then: (1) `curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health` → expect **200**; (2) `curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/patients/1` → expect **401** (no token; confirms the request went through the gateway). See [SECURITY-GATEWAY.md](SECURITY-GATEWAY.md) for the full runbook.

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
> responsibility to generate and mount the certificate/key (or use the dev script below).

### T1.2 – Activer HTTPS

- **Production**: Provide your own keystore (PKCS12/JKS) and set the environment variables (`SERVER_SSL_ENABLED=true`, `SERVER_SSL_KEY_STORE`, `SERVER_SSL_KEY_STORE_PASSWORD`, `SERVER_SSL_KEY_STORE_TYPE`, `SERVER_SSL_KEY_ALIAS`). Mount the keystore in Docker or point to its path.
- **Dev / démo**: Run the script to generate a self-signed keystore:
  - From `gateway-service`: `./scripts/generate-dev-keystore.sh` (or `scripts\generate-dev-keystore.bat` on Windows). This creates `build/gateway-dev.p12`.
  - Set `SERVER_SSL_ENABLED=true`, `SERVER_SSL_KEY_STORE=<path-to>/gateway-dev.p12`, `SERVER_SSL_KEY_STORE_PASSWORD=changeit`, `SERVER_SSL_KEY_STORE_TYPE=PKCS12`, `SERVER_SSL_KEY_ALIAS=gateway`.
  - Start the gateway; it will listen on HTTPS on port 8080.
- **Vérification**: `curl -k https://localhost:8080/actuator/health` → **200**. `curl -k https://localhost:8080/api/patients/1` → **401**. Use `-k` to accept the self-signed certificate in dev.
- **HSTS avec HTTPS**: When TLS is on, activate the `tls` profile so HSTS is sent (e.g. `SPRING_PROFILES_ACTIVE=docker,tls` or `spring.profiles.include=tls`). The profile is defined in `application-tls.yml` and re-enables the `Strict-Transport-Security` header.

**Redirection HTTP vers HTTPS (T1.2)**  
When HTTPS is enabled, the gateway listens **only on HTTPS** (port 8080). There is no HTTP listener on the gateway; clients must use `https://`. For HTTP→HTTPS redirect (e.g. users typing `http://`), use a reverse proxy or load balancer in front of the gateway: listen on port 80 and respond with **301** or **302** to `https://<host>:8080/...` (or to 443 if the proxy terminates TLS). Spring Cloud Gateway (Netty) does not support two listeners (HTTP + HTTPS) in the same process out of the box.

**Docker avec TLS (T1.2)**  
When TLS is enabled in Docker, set the SSL environment variables and mount the keystore (e.g. a volume pointing to the host path of your PKCS12 file). The gateway healthcheck must use **HTTPS** (and, for self-signed certs, ignore certificate verification). An optional override file **`docker-compose.tls.yml`** at the project root is provided: it overrides the gateway with TLS env, mounts `gateway-service/build` as the keystore directory (after running `./scripts/generate-dev-keystore.sh`), and sets the healthcheck to `wget ... https://localhost:8080/actuator/health` with `--no-check-certificate`. Run with: `docker-compose -f docker-compose.yml -f docker-compose.tls.yml up -d --build`. Verify with `curl -k https://localhost:8080/actuator/health`.

---

## 4. JWT verification

The gateway verifies a JWT on **every request** except for public paths.

- **Algorithms** (T1.3):
  - **RS256** (recommended for production): When a **public key** is provided, the gateway verifies tokens with RS256. The auth-service signs tokens with the corresponding **private key** (from its own Secrets). The gateway only needs the **public key** (from Secrets: env `JWT_PUBLIC_KEY` or file `JWT_PUBLIC_KEY_LOCATION`). No shared secret is required.
  - **HS256** (default when no public key is set): Tokens are verified using a **shared secret** (`jwt.secret` / `JWT_SECRET`), same as auth-service.
- **Configuration**:
  - **RS256**: Set `JWT_PUBLIC_KEY` to the PEM string (e.g. from Secrets), or `JWT_PUBLIC_KEY_LOCATION` to a file path (mounted from Secrets). In `application.yml`: `jwt.public-key` and `jwt.public-key-location` (empty by default).
  - **HS256**: `jwt.secret` must be the same Base64-encoded secret as in auth-service; override with `JWT_SECRET` in Docker/Secrets.
- **Key generation (RS256)**: Use the provided script to generate an RSA key pair. Give the **private key** to auth-service (Secrets: `JWT_PRIVATE_KEY` or `JWT_PRIVATE_KEY_LOCATION`) and the **public key** to the gateway (Secrets: `JWT_PUBLIC_KEY` or `JWT_PUBLIC_KEY_LOCATION`).
  - From project root: `./gateway-service/scripts/generate-jwt-rs256-keys.sh` (Linux/macOS) or `gateway-service\scripts\generate-jwt-rs256-keys.bat` (Windows, requires OpenSSL). Output: `gateway-service/build/jwt-keys/private.pem` (auth), `public.pem` (gateway). **Do not commit** `private.pem`.
- **Public paths** (no token required):
  - `/api/auth/login`
  - `/api/auth/register`
  - `/api/auth/refresh`
  - `/actuator/health`
- **Protected paths**: Any other path (e.g. `/api/patients/**`, `/api/staff/**`) requires a valid `Authorization: Bearer <token>` header. If the header is missing, not Bearer, or the token is invalid/expired, the gateway responds with **401 Unauthorized** and a JSON body `{"error":"..."}`. Response headers include `Content-Type: application/json` and `WWW-Authenticate: Bearer`.
- **Forwarded headers** (when the token is valid): The gateway adds the following headers to the request before forwarding to downstream services (so microservices can use them without parsing the JWT):
  - `X-User-Id`: user ID from the token
  - `X-Username`: subject (username)
  - `X-User-Roles`: comma-separated list of roles

**Shared secret (HS256 only)**: In development, the default `jwt.secret` in `application.yml` matches auth-service. In Docker/production with HS256, set the same `JWT_SECRET` for both gateway-service and auth-service. When using RS256, the gateway uses only the public key and does not need `jwt.secret`.

---

## 5. RBAC on patient dossiers (US1.3 / T1.4)

After authentication, the gateway applies **role-based access control (RBAC)** on requests to patient-dossier resources. The **RBAC engine** (T1.4) evaluates rules by role, resource, and action; the allow/deny decision is taken **before** the request is proxied to the backend. This is enforced only in the gateway; no change is made to KIT COMMUN controllers or business logic.

### 5.1 Paths under RBAC

Only the following path prefixes are subject to this RBAC:

- **`/api/patients/**`** – patient CRUD and search
- **`/api/medical-records/**`** – medical records
- **`/api/consultations/**`** – consultations

All other paths (e.g. `/api/auth/**`, `/api/staff/**`, `/api/appointments/**`) are **not** checked by this RBAC filter; they are only subject to authentication (valid JWT).

### 5.2 Role / permission matrix

Actions are derived from the HTTP method: **GET → READ**, **POST → CREATE**, **PUT/PATCH → UPDATE**, **DELETE → DELETE**.

| Resource          | READ | CREATE | UPDATE | DELETE |
|-------------------|------|--------|--------|--------|
| **PATIENTS**      | ADMIN, MEDECIN, DOCTOR, INFIRMIER, NURSE, RECEPTIONIST, LAB_TECH | ADMIN, MEDECIN, DOCTOR, NURSE, RECEPTIONIST | ADMIN, MEDECIN, DOCTOR, NURSE | ADMIN only |
| **MEDICAL_RECORDS** | ADMIN, MEDECIN, DOCTOR, INFIRMIER, NURSE, LAB_TECH | ADMIN, MEDECIN, DOCTOR, NURSE, LAB_TECH | ADMIN, MEDECIN, DOCTOR, NURSE | ADMIN only |
| **CONSULTATIONS** | ADMIN, MEDECIN, DOCTOR, INFIRMIER, NURSE | ADMIN, MEDECIN, DOCTOR, NURSE | ADMIN, MEDECIN, DOCTOR, NURSE | ADMIN only |

Role names in the JWT (and in `X-User-Roles`) must match the auth-service `Role` enum: `ROLE_ADMIN`, `ROLE_MEDECIN`, `ROLE_DOCTOR`, `ROLE_INFIRMIER`, `ROLE_NURSE`, `ROLE_RECEPTIONIST`, `ROLE_LAB_TECH`, `ROLE_PATIENT`. **ROLE_PATIENT** has **no** access to these patient-dossier paths in the current policy (no “own dossier only” claim in the JWT by default).

### 5.3 Denied access: 403 and audit

- If the authenticated user’s roles do **not** allow the requested action on the resource, the gateway:
  1. Responds with **403 Forbidden**.
  2. Sets **`Content-Type: application/json`** and a JSON body: `{"error":"Forbidden"}` (generic message).
  3. Does **not** call the downstream service (request is not proxied).
  4. Emits an **ACCESS_DENIED** audit event (see below).

Filter order: **AuthenticationFilter** (order -100) runs first (401 if no/invalid token); **RbacAuthorizationFilter** (order -50) runs next (403 if not allowed).

### 5.4 ACCESS_DENIED audit event

Every 403 from the RBAC filter triggers an **ACCESS_DENIED** event. The payload is **pseudonymised** (no PII/PHI: no patient name, no diagnosis).

| Field          | Description |
|----------------|-------------|
| `eventType`    | `"ACCESS_DENIED"` |
| `timestamp`    | ISO-8601 instant |
| `userId`       | Technical user ID (from JWT / `X-User-Id`) |
| `resourceType` | `PATIENTS` \| `MEDICAL_RECORDS` \| `CONSULTATIONS` |
| `resourceId`   | ID extracted from path when applicable (e.g. patient or record id), or empty |
| `action`       | `READ` \| `CREATE` \| `UPDATE` \| `DELETE` |
| `reason`       | e.g. `"RBAC_DENY"` |

**Sending the event**

- **By default**, no external service is called: the gateway uses a **no-op** implementation of the audit sender (optional DEBUG log only).
- To send events to a **security-audit-log** service, set in configuration:
  - **`security.audit.url`** to the audit endpoint URL (e.g. `http://security-audit-log:8080/api/events`).  
  When this property is set, the gateway uses an HTTP implementation that POSTs the JSON payload to that URL (fire-and-forget, non-blocking). If the service is not yet deployed, leave the property unset so that the no-op remains in use.

---

## 6. Rate limiting (US1.4)

The gateway applies **configurable rate limits** by **client IP** and by **authenticated user** to reduce abuse. When a limit is exceeded, the gateway returns **429 Too Many Requests** and emits a **RATE_LIMIT_EXCEEDED** event (for IDS/audit).

### 6.1 Limits applied

- **Per IP**: Every request (including unauthenticated ones, e.g. login/register) counts against the **per-IP** limit. The client IP is taken from the **`X-Forwarded-For`** header (first value) when present, otherwise from the request’s remote address.
- **Per user**: For requests that already have **`X-User-Id`** (set by the AuthenticationFilter after a valid JWT), an additional **per-user** limit is applied. Both limits must be satisfied for the request to proceed.

### 6.2 Configuration

In `application.yml` (and via environment variables in Docker):

| Property | Description | Default |
|----------|-------------|---------|
| `rate-limit.requests-per-minute-per-ip` | Max requests per window per client IP | 100 |
| `rate-limit.requests-per-minute-per-user` | Max requests per window per authenticated user | 100 |
| `rate-limit.window-seconds` | Window duration in seconds | 60 |
| `rate-limit.excluded-paths` | Path prefixes that do not consume quota (e.g. health checks) | `/actuator/health` |

Example env vars: `RATE_LIMIT_REQUESTS_PER_MINUTE_PER_IP`, `RATE_LIMIT_REQUESTS_PER_MINUTE_PER_USER`, `RATE_LIMIT_WINDOW_SECONDS`.

### 6.3 When limit is exceeded: 429 and event

- The gateway responds with **429 Too Many Requests**, **`Content-Type: application/json`**, and body: `{"error":"Too Many Requests"}`.
- The request is **not** proxied to the backend.
- A **RATE_LIMIT_EXCEEDED** event is sent (see below) in a non-blocking way (fire-and-forget).

**Filter order**: **RateLimitFilter** runs with order **-90**, i.e. after **AuthenticationFilter** (-100) and before **RbacAuthorizationFilter** (-50).

### 6.4 RATE_LIMIT_EXCEEDED event (IDS)

Each 429 triggers a **RATE_LIMIT_EXCEEDED** event. The payload is suitable for IDS/audit (key type and key identify the limit that was exceeded).

| Field | Description |
|-------|-------------|
| `eventType` | `"RATE_LIMIT_EXCEEDED"` |
| `timestamp` | ISO-8601 instant |
| `keyType` | `"IP"` or `"USER"` |
| `key` | Client IP or user ID (technical identifier) |
| `limit` | Configured limit (requests per window) |
| `windowSeconds` | Configured window in seconds |

**Sending the event**

- **By default**, the same **no-op** implementation as for ACCESS_DENIED is used (DEBUG log only).
- When **`security.audit.url`** is set, the HTTP implementation POSTs both ACCESS_DENIED and RATE_LIMIT_EXCEEDED events to that URL. IDS or audit-log can consume the same endpoint.

### 6.5 Implementation notes

- Rate limiting uses an **in-memory** store (per gateway instance). With multiple gateway instances, limits are **per instance**, not global.
- **X-Forwarded-For**: When the gateway is behind a proxy, the first value in the header is used as the client IP. Document trust boundaries (e.g. only trust when the gateway is not directly exposed) to avoid spoofing.

---

## 7. Strict input validation (US1.6)

The gateway applies **strict validation** of request inputs (query parameters and headers) to block injection attempts (SQLi, XSS). Suspicious requests receive **400 Bad Request** and trigger a **SUSPICIOUS_INPUT** event for IDS.

### 7.1 Validation schemas

- **Structural validation**: Request body and field formats are validated in each microservice via **Bean Validation** (`@Valid`, `@NotBlank`, etc.) on DTOs. Controllers return **400** for `MethodArgumentNotValidException`.
- **Injection detection**: In the gateway, **query parameter values** and **header values** (except `Authorization`, to avoid false positives on JWT) are scanned for SQLi/XSS-like patterns (blocklist). This is a separate layer from Bean Validation; no change to existing DTO rules.

### 7.2 Scope and behaviour

- **Scanned**: All query parameter values and all header values except `Authorization`.
- **Body**: Not inspected in the gateway in the current implementation (no body buffering).
- **When a pattern matches**: The gateway responds with **400 Bad Request**, **`Content-Type: application/json`**, and body: `{"error":"Invalid or suspicious input"}`. The request is **not** proxied. A **SUSPICIOUS_INPUT** audit event is sent (see below). The response does **not** reveal which pattern matched.
- **Excluded paths**: Same as rate limit, e.g. `/actuator/health`; configurable via `input-validation.excluded-paths`.

**Filter order**: **InputValidationFilter** runs with order **-80**, i.e. after **RateLimitFilter** (-90) and before **RbacAuthorizationFilter** (-50).

### 7.3 Configuration

In `application.yml`:

| Property | Description | Default |
|----------|-------------|---------|
| `input-validation.enabled` | Enable/disable the input validation filter | `true` |
| `input-validation.excluded-paths` | Path prefixes that skip injection checks | `/actuator/health` |

Example env var: `INPUT_VALIDATION_ENABLED`.

### 7.4 SUSPICIOUS_INPUT event (IDS)

Each 400 from the input validation filter triggers a **SUSPICIOUS_INPUT** event. The payload contains **no PII** and no raw input value (suitable for IDS).

| Field | Description |
|-------|-------------|
| `eventType` | `"SUSPICIOUS_INPUT"` |
| `timestamp` | ISO-8601 instant |
| `source` | `"query"` or `"header"` (where the pattern was detected) |
| `path` | Request path (e.g. `/api/patients/search`) |
| `method` | HTTP method (e.g. `GET`) |
| `category` | `"SQLI"` or `"XSS"` (detection category) |

**Sending the event**

- The same **no-op** / **HTTP** behaviour as for ACCESS_DENIED and RATE_LIMIT_EXCEEDED applies: when **`security.audit.url`** is set, the gateway POSTs SUSPICIOUS_INPUT to that URL (fire-and-forget).

---

## 8. Security response headers (US1.7)

The gateway adds **security headers** to all responses (gateway-generated and proxied) using Spring Cloud Gateway’s built-in **SecureHeaders** filter applied via `default-filters`. Every client-facing response includes the required headers.

### 8.1 Headers added

| Header | Value | Description |
|--------|--------|-------------|
| **X-Content-Type-Options** | `nosniff` | Prevents MIME-type sniffing (required by US1.7). |
| **X-Frame-Options** | `DENY` | Prevents clickjacking (required by US1.7). |
| **Strict-Transport-Security** (HSTS) | `max-age=...` | Enforces HTTPS on subsequent visits (required by US1.7). Sent only when not disabled (see below). |
| X-XSS-Protection | `1; mode=block` | Legacy XSS filter hint. |
| Referrer-Policy | `no-referrer` | Controls referrer information. |
| Content-Security-Policy | (default) | Restricts resource loading. |
| X-Download-Options | `noopen` | Prevents opening downloads in browser context. |
| X-Permitted-Cross-Domain-Policies | `none` | Restricts cross-domain policy. |

### 8.2 Implementation and configuration

- **Implementation**: In `application.yml`, `spring.cloud.gateway.default-filters` includes `SecureHeaders`, so the filter runs for every route. No custom Java filter is required.
- **HSTS when SSL is disabled**: By default, **Strict-Transport-Security** is **disabled** via `spring.cloud.gateway.filter.secure-headers.disable: strict-transport-security` so that when the gateway serves HTTP (e.g. local/dev), HSTS is not sent. When you enable HTTPS (e.g. in production), remove `strict-transport-security` from the `disable` list (or override in a profile) so that HSTS is sent.
- **Customisation**: Header values can be overridden under `spring.cloud.gateway.filter.secure-headers` (e.g. `strict-transport-security`, `frame-options`, `content-type-options`). See [SecureHeaders GatewayFilter Factory](https://docs.spring.io/spring-cloud-gateway/reference/spring-cloud-gateway-server-webflux/gatewayfilter-factories/secureheaders-factory.html).

### 8.3 Verification

- **Manual**: `curl -I http://localhost:8080/api/patients/1` — expect 401 with `X-Content-Type-Options`, `X-Frame-Options` (and other secure headers). HSTS will be absent when the default config disables it.

---

## 9. Routing configuration

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

## 10. Tests

Main test classes:

1. **AuthenticationFilterTest**
   - Public paths (`/api/auth/login`, `/api/auth/register`, `/api/auth/refresh`,
     `/actuator/health`) are allowed without a token; `chain.filter` is called.
   - Private path without token → **401**, `chain.filter` not called.
   - Private path with invalid or malformed token → **401**, `chain.filter` not called.
   - Private path with valid token (mocked claims) → `chain.filter` called with mutated request containing `X-User-Id`, `X-Username`, `X-User-Roles`.

2. **RbacServiceTest** / **RbacAuthorizationFilterTest**
   - RBAC service: path outside patient resources → allowed; allowed role on patient path → allowed; disallowed role (e.g. PATIENT on GET `/api/patients`) → denied; `resolveResource`, `resolveAction`, `extractResourceId` behaviour.
   - RBAC filter: path out of scope → chain called; allowed role on patient path → chain called, no 403; denied role → 403, JSON body `{"error":"Forbidden"}`, chain not called, `SecurityAuditSender.sendAccessDenied` invoked (mock). No `X-Username` on patient path → chain called (RBAC skips).

3. **RateLimitStoreTest** / **RateLimitFilterTest**
   - Rate limit store: under limit → allowed; over limit in window → denied; different keys independent; after window expires → allowed again.
   - Rate limit filter: excluded path → chain called, no quota consumed; under limit → chain called; over IP limit → 429, JSON body, `SecurityAuditSender.sendRateLimitExceeded` invoked with keyType IP; over user limit (with `X-User-Id`) → 429, audit with keyType USER.

4. **InjectionPatternsTest** / **InputValidationFilterTest**
   - Injection patterns: known SQLi (e.g. `OR 1=1`, `UNION SELECT`) → match SQLI; known XSS (e.g. `<script>`, `javascript:`) → match XSS; safe input → no match.
   - Input validation filter: excluded path → chain called; suspicious query param → 400, JSON body, `SecurityAuditSender.sendSuspiciousInput` invoked with source=query, category=SQLI or XSS; suspicious header → 400, audit with source=header; safe input → chain called; disabled → chain called even with suspicious input.

5. **JwtVerificationServiceTest**
   - Valid token (same secret as auth-service) → claims returned (username, roles, userId, staffId).
   - Expired, malformed, or wrong-secret token → `Optional.empty()`.
   - Null or blank token → `Optional.empty()`.

6. **GatewayRoutesConfigTest**
   - Spring Boot test that starts the gateway context and autowires the `RouteLocator`.
   - Verifies that the gateway defines routes for **all** core KIT COMMUN services:
     `auth-service`, `patient-service`, `staff-service`,
     `appointment-service`, `medical-record-service`, `consultations-service`.

These tests ensure that:

- The gateway intercepts and filters all traffic.
- All API paths of the KIT COMMUN are reachable **through** the gateway configuration.

For full end-to-end verification in Docker:

1. Start the stack with `docker-compose up -d --build`.
2. **Without a token**, a protected path must return 401:
   ```bash
   curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/patients/1
   # Expect 401
   ```
3. **With a valid token** (obtain via `POST http://localhost:8080/api/auth/login`), the same path should be forwarded and return 200 or 404 from the backend; if the user’s role is not allowed for that action on a patient-dossier path, the gateway returns **403** (no call to the backend) and emits an ACCESS_DENIED audit event. If the client or user exceeds the rate limit, the gateway returns **429** and emits a RATE_LIMIT_EXCEEDED event. If a query or header contains SQLi/XSS-like patterns (e.g. `?query=OR 1=1`), the gateway returns **400** and emits a SUSPICIOUS_INPUT event.
   ```bash
   curl -H "Authorization: Bearer <accessToken>" http://localhost:8080/api/patients/1
   ```
4. Verify that there is **no direct access** to the microservices on ports 8081–8086 from the host (connections should fail), confirming that all external traffic must go through the gateway.

