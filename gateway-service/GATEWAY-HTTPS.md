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

## 4. JWT verification

The gateway verifies a JWT on **every request** except for public paths.

- **Algorithm**: HS256 (same as auth-service). Tokens are issued by auth-service and verified by the gateway using a **shared secret**.
- **Configuration**: In `application.yml`, `jwt.secret` must be the same Base64-encoded secret as in auth-service. It can be overridden with the `JWT_SECRET` environment variable (e.g. in Docker) so both services use the same value.
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

**Shared secret**: In development, the default `jwt.secret` in `application.yml` matches auth-service. In Docker/production, set the same `JWT_SECRET` (or equivalent) for both gateway-service and auth-service so that tokens issued by auth-service are accepted by the gateway.

---

## 5. RBAC on patient dossiers (US1.3)

After authentication, the gateway applies **role-based access control (RBAC)** on requests to patient-dossier resources. This is enforced only in the gateway; no change is made to KIT COMMUN controllers or business logic.

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

## 6. Routing configuration

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

## 7. Tests

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

3. **JwtVerificationServiceTest**
   - Valid token (same secret as auth-service) → claims returned (username, roles, userId, staffId).
   - Expired, malformed, or wrong-secret token → `Optional.empty()`.
   - Null or blank token → `Optional.empty()`.

4. **GatewayRoutesConfigTest**
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
3. **With a valid token** (obtain via `POST http://localhost:8080/api/auth/login`), the same path should be forwarded and return 200 or 404 from the backend; if the user’s role is not allowed for that action on a patient-dossier path, the gateway returns **403** (no call to the backend) and emits an ACCESS_DENIED audit event.
   ```bash
   curl -H "Authorization: Bearer <accessToken>" http://localhost:8080/api/patients/1
   ```
4. Verify that there is **no direct access** to the microservices on ports 8081–8086 from the host (connections should fail), confirming that all external traffic must go through the gateway.

