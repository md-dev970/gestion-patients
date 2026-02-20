# Auth Service – Security (US1.5)

## Anti-bruteforce protection on login

The auth service applies **configurable anti-bruteforce** protection on `POST /api/auth/login`:

- After **N** consecutive failed login attempts (wrong password) for a given username, the account is **temporarily locked**.
- While locked, any login attempt for that user returns **423 Locked** with a JSON body: `{"error":"Account temporarily locked"}`.
- After the **lockout duration** has passed, the next login attempt (success or failure) **unlocks** the account (counter and lock are reset); a failed attempt then increments the counter again.
- On **successful** login, the failed-attempt counter and lock are **reset**.

### Configuration

| Property | Description | Default |
|----------|-------------|---------|
| `auth.bruteforce.max-failed-attempts` | Number of failed attempts before lock | 5 |
| `auth.bruteforce.lockout-duration-minutes` | Lockout duration in minutes | 15 |

Environment variables: `AUTH_BRUTEFORCE_MAX_FAILED_ATTEMPTS`, `AUTH_BRUTEFORCE_LOCKOUT_DURATION_MINUTES`.

### ACCOUNT_LOCKED event (IDS / audit)

When an account is locked (Nth failed attempt), the service sends an **ACCOUNT_LOCKED** event toward IDS/audit. The payload is suitable for security monitoring (no PHI beyond technical identifiers).

| Field | Description |
|-------|-------------|
| `eventType` | `"ACCOUNT_LOCKED"` |
| `timestamp` | ISO-8601 instant |
| `userId` | Technical user ID |
| `username` | Username (identifier) |
| `reason` | e.g. `"BRUTEFORCE"` |

**Sending the event**

- **By default**, no external service is called (no-op implementation; optional DEBUG log).
- When **`security.audit.url`** is set in configuration, the service POSTs the JSON payload to that URL (fire-and-forget). The same URL can be used by the gateway for ACCESS_DENIED and RATE_LIMIT_EXCEEDED events.
