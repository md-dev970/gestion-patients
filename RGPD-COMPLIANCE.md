# GDPR / HIPAA Compliance Notes

This document describes the project-level consent model, legal bases, audit trail, and data-subject workflows implemented in the hospital patient microservices platform.

The file name keeps the original French acronym `RGPD`, but the content is written in English. GDPR is the European data-protection regulation; HIPAA is referenced as a healthcare-security benchmark for protected health information.

## 1. Scope

The compliance notes cover:

- Patient identity and demographic data managed by `patient-service`.
- Medical records and entries managed by `medical-record-service`.
- Consultations managed by `consultations-service`.
- Appointments managed by `appointment-service`.
- User accounts and authentication data managed by `auth-service`.
- Gateway-level access control, security events, and filtering.

The implementation avoids placing raw PII/PHI in audit and IDS events. Event payloads contain technical identifiers such as `patientId`, `resourceId`, and `userId`.

## 2. Consent Model

Patient records include consent-related fields:

| Field | Purpose | Typical Values |
|-------|---------|----------------|
| `consentGiven` | Indicates whether the patient has consented to data processing | `true`, `false` |
| `legalBasis` | Legal basis for processing under GDPR Article 6 | `consent`, `legitimate_interest`, `contract`, `legal_obligation`, `withdrawn` |

When a patient is created and no `legalBasis` is provided, the service defaults it to `consent`. This ensures every patient record starts with an explicit processing basis.

## 3. Consent Withdrawal

`patient-service` exposes:

```http
PUT /api/patients/{id}/consent/withdraw
```

The withdrawal flow:

1. Sets `consentGiven` to `false`.
2. Sets `legalBasis` to `withdrawn`.
3. Emits a `PHI_ACCESS` audit event with action `UPDATE`.
4. Returns the updated patient DTO.

After consent is withdrawn, processing that relies only on consent should stop. Data already collected may still be retained when another lawful basis applies, such as legal obligation, continuity of care, or retention requirements.

## 4. Legal Bases

| Legal Basis | Typical Use |
|-------------|-------------|
| `consent` | Explicit patient consent for data processing |
| `legitimate_interest` | Hospital continuity of care or operational necessity |
| `contract` | Contractual relationship, such as insurance or service agreement |
| `legal_obligation` | Statutory retention, reporting, or regulatory duty |
| `withdrawn` | Consent has been withdrawn; consent-based processing must stop |

In a production deployment, the controller/service behavior should be supported by an internal register of processing activities and policy documentation owned by the data-protection function.

## 5. Data-Subject Rights

### Right Of Access

Patients and authorized staff can access an aggregated patient dossier through:

```http
GET /api/patients/{id}/dossier
```

The dossier aggregates:

- Patient data from `patient-service`.
- Medical record data from `medical-record-service`.
- Consultation history from `consultations-service`.
- Appointment data from `appointment-service`.

Access is controlled by the gateway RBAC policy. Dossier access emits a `DOSSIER_ACCESSED` event.

### Right To Portability / Export

The dossier can be exported as JSON:

```http
GET /api/patients/{id}/dossier/export
```

The response is JSON and includes `Content-Disposition: attachment`.

### Right To Erasure

Patient deletion is available through:

```http
DELETE /api/patients/{id}
```

`patient-service` orchestrates cascade deletion across:

- `medical-record-service`
- `consultations-service`
- `appointment-service`

The gateway allows:

- Admin deletion by `ROLE_ADMIN`.
- Patient self-deletion when the requester has `ROLE_PATIENT` and the path ID matches `X-User-Id`.

When self-deletion is allowed, the gateway emits `PATIENT_SELF_DELETION_REQUESTED` before proxying the request.

### Account Anonymization And Deletion

`auth-service` exposes:

```http
PUT /api/auth/account/{userId}/anonymize
DELETE /api/auth/account/{userId}
```

Anonymization replaces identifying account data with non-reversible values. Deletion removes the account record when no required references remain.

## 6. Retention And Purge

`patient-service` contains a scheduled retention purge job:

| Property | Purpose | Default |
|----------|---------|---------|
| `retention.patient-years` | Patient retention duration in years | `10` |
| `retention.purge.enabled` | Enables scheduled purge | `false` |
| `retention.purge.cron` | Purge schedule | `0 0 2 * * ?` |

When enabled, the purge job deletes patients whose retention date has passed and uses the normal cascade deletion path. A `RETENTION_PURGE` event is emitted after a purge run.

## 7. Auditability For DPO / Privacy Officers

The system emits audit events for sensitive operations:

| Event | Meaning |
|-------|---------|
| `PHI_ACCESS` | Successful read, create, or update of protected health information |
| `PHI_DELETED` | Successful deletion of protected health information |
| `DOSSIER_ACCESSED` | Patient dossier was read or exported |
| `RETENTION_PURGE` | Scheduled retention purge completed |
| `PATIENT_SELF_DELETION_REQUESTED` | Patient initiated deletion of their own record |
| `ACCESS_DENIED` | Gateway RBAC denied access |
| `RATE_LIMIT_EXCEEDED` | Rate limit or login-abuse threshold exceeded |
| `SUSPICIOUS_INPUT` | Gateway rejected SQLi/XSS-like query or header content |
| `ACCOUNT_LOCKED` | Auth service locked an account after repeated failed login attempts |

The DPO can use these events to answer:

- Who accessed a patient record or dossier.
- When access occurred.
- Which technical resource was affected.
- Whether data was exported.
- Whether deletion was initiated by an admin, retention job, or patient self-service.

See [AUDIT-IDS-EVENTS.md](AUDIT-IDS-EVENTS.md) for payload definitions.

## 8. PII / PHI Minimization

Audit and IDS payloads must not include:

- Patient names.
- Email addresses.
- Phone numbers.
- Diagnoses.
- Prescriptions.
- Notes.
- Raw request payloads.
- Suspicious raw input strings.

They may include:

- Technical user IDs.
- Resource IDs.
- Patient IDs.
- Action types.
- Event categories.
- Timestamps.

## 9. Security Controls Supporting Compliance

The gateway and services provide:

- JWT authentication.
- Gateway RBAC before backend proxying.
- Secure response headers.
- Gateway rate limiting.
- Account and IP anti-bruteforce controls.
- Query/header input validation.
- Optional audit and IDS event delivery.
- HTTPS support at the gateway.
- Per-service database isolation.

See [SECURITY-ENCRYPTION.md](SECURITY-ENCRYPTION.md) and [gateway-service/GATEWAY-HTTPS.md](gateway-service/GATEWAY-HTTPS.md).

## 10. Production Recommendations

- Replace development secrets with a secrets manager.
- Prefer RS256 JWT signing with private key in `auth-service` and public key in `gateway-service`.
- Enable HTTPS with a trusted certificate.
- Encrypt database volumes or use managed PostgreSQL encryption at rest.
- Configure `security.audit.url` and `security.ids.url` to durable storage.
- Define retention policies for audit logs and backups.
- Use database migrations instead of `ddl-auto: update` in production.
- Document processing activities under GDPR Article 30.
- Add operational runbooks for access requests, rectification, export, deletion, and incident response.
