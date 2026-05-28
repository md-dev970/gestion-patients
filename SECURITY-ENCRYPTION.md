# Encryption And Secure Data Disposal

This document describes how the platform protects data in transit, how data at rest should be protected, and how data should be securely disposed of after deletion or retention purge.

## 1. Encryption In Transit

All client-facing API traffic should pass through `gateway-service`.

Development default:

```text
http://localhost:8080
```

HTTPS mode:

```text
https://localhost:8080
```

The gateway supports TLS through Spring Boot `server.ssl` configuration:

| Property | Purpose |
|----------|---------|
| `SERVER_SSL_ENABLED` | Enables HTTPS when set to `true` |
| `SERVER_SSL_KEY_STORE` | Keystore path |
| `SERVER_SSL_KEY_STORE_PASSWORD` | Keystore password |
| `SERVER_SSL_KEY_STORE_TYPE` | Usually `PKCS12` |
| `SERVER_SSL_KEY_ALIAS` | Certificate alias |

For local testing, generate a self-signed keystore:

Windows:

```powershell
cd gateway-service
.\scripts\generate-dev-keystore.bat
```

Linux or macOS:

```bash
cd gateway-service
./scripts/generate-dev-keystore.sh
```

Start Docker with the TLS override:

```bash
docker-compose -f docker-compose.yml -f docker-compose.tls.yml up -d --build
```

Verify:

```bash
curl -k https://localhost:8080/actuator/health
```

In production, use a certificate issued by a trusted CA and enable HSTS through the TLS profile.

## 2. Service-To-Service Traffic

In the Docker deployment, microservices communicate over the internal `hospital-network` bridge network. The default project configuration uses HTTP between services and Eureka.

For higher-security environments, consider:

- mTLS between services.
- A service mesh.
- Network policies that restrict lateral movement.
- Separate private subnets for databases.
- TLS to managed PostgreSQL instances.

## 3. Encryption At Rest

The project uses one PostgreSQL database per service:

| Service | Database |
|---------|----------|
| `auth-service` | `hospital_auth` |
| `patient-service` | `hospital_patients` |
| `staff-service` | `hospital_staff` |
| `appointment-service` | `hospital_appointments` |
| `medical-record-service` | `hospital_medical_records` |
| `consultations-service` | `hospital_consultations` |

Application-level behavior:

- User passwords are not stored in plain text. `auth-service` uses a `PasswordEncoder`.
- Audit events avoid raw PII/PHI payloads.
- Docker development databases are not encrypted by the application.

Production recommendations:

- Enable volume encryption, such as BitLocker, LUKS, cloud disk encryption, or managed database encryption.
- Use a secrets manager for database credentials and JWT keys.
- Restrict database network access to the owning service.
- Configure backups with encryption and access controls.
- Avoid logging SQL values that may contain PHI.
- Replace Hibernate `ddl-auto: update` with explicit migrations.

## 4. JWT Key Protection

The gateway supports HS256 and RS256:

- HS256 uses a shared `JWT_SECRET`. It is simpler for development but requires the same secret in both `auth-service` and `gateway-service`.
- RS256 is preferred for production. The private key stays in `auth-service`; the gateway only receives the public key.

Generate development RSA keys:

Windows:

```powershell
gateway-service\scripts\generate-jwt-rs256-keys.bat
```

Linux or macOS:

```bash
./gateway-service/scripts/generate-jwt-rs256-keys.sh
```

Do not commit generated private keys.

## 5. Secure Data Disposal

Data disposal happens through hard deletion in the current implementation.

### Patient Deletion

`DELETE /api/patients/{id}` triggers cascade deletion of patient-related data:

1. Medical records and entries.
2. Consultations.
3. Appointments.
4. Patient record.

Each participating service can emit `PHI_DELETED` after successful deletion.

### Retention Purge

`patient-service` includes `RetentionPurgeJob`, controlled by:

| Property | Purpose |
|----------|---------|
| `retention.patient-years` | Retention duration |
| `retention.purge.enabled` | Enables the scheduled job |
| `retention.purge.cron` | Cron expression |

The purge deletes expired patient records through the same cascade path and emits `RETENTION_PURGE`.

### Account Disposal

`auth-service` supports:

- Account anonymization, which replaces identifying data with non-reversible values.
- Account deletion, which removes the user row when allowed.

## 6. Backups And Residual Data

Hard deletion from application tables does not automatically remove data from:

- Database backups.
- WAL/archive logs.
- Filesystem snapshots.
- Observability logs.
- Data exports.

Production disposal policy should define:

- Backup retention duration.
- Backup encryption.
- Backup destruction process.
- Snapshot expiration.
- Audit evidence for deletion requests.
- Procedures for restoring deleted records from backups only when legally justified.

## 7. Operational Checklist

- Use HTTPS for all client traffic.
- Prefer RS256 JWT signing in production.
- Store secrets outside Git and outside Compose files.
- Encrypt database volumes and backups.
- Configure audit/IDS endpoints.
- Verify that audit events contain only technical identifiers.
- Enable retention purge only after confirming policy and test data.
- Document secure backup destruction.
