# Spring Boot Auth Template

A production-ready authentication and authorization scaffold. Copy it, rename the package, and add your domain logic.

## Tech Stack

| Concern | Choice |
|---|---|
| Framework | Spring Boot 3.5.x, Java 21 |
| Security | Spring Security 6 — stateless JWT filter chain |
| Database | PostgreSQL 16 via Spring Data JPA |
| Migrations | Flyway (Hibernate set to `validate`, never auto-DDL) |
| JWT | jjwt 0.12.x (HMAC-SHA256) |
| Mapping | MapStruct |
| Email | Spring Mail / JavaMailSender |

---

## Quick Start

### 1. Start infrastructure

```bash
docker-compose up -d
```

This starts:
- **PostgreSQL** on `localhost:5432` (db=`trackitdb`, user/pass=`template`)
- **MailHog** SMTP on `localhost:1025`, web UI on `http://localhost:8025`

Flyway runs automatically on startup and applies V1–V3 migrations.

### 2. Run the application

```bash
# From the backend/ directory
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

The app starts on **http://localhost:8080**.

The `dev` profile activates:
- MailHog SMTP (no auth, no TLS)
- SQL logging
- `DevDataSeeder` — seeds `admin@example.com` / `Admin@123456` with `ROLE_ADMIN` + `ROLE_USER`

### 3. Verify

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@example.com","password":"Admin@123456"}' | jq
```

---

## API Reference

### Auth endpoints

| Method | Path | Auth required | Description |
|---|---|---|---|
| POST | `/api/auth/register` | No | Register; sends confirmation email |
| GET | `/api/auth/confirm?token=` | No | Confirm email address |
| POST | `/api/auth/login` | No | Returns access + refresh token |
| POST | `/api/auth/logout` | No | Revokes refresh token |
| POST | `/api/auth/refresh` | No | Returns new access token |
| POST | `/api/auth/forgot-password` | No | Sends reset email |
| POST | `/api/auth/reset-password` | No | Resets password via token |
| POST | `/api/auth/change-password` | JWT | Changes password (requires current) |

### User endpoints

| Method | Path | Auth required | Description |
|---|---|---|---|
| GET | `/api/users/me` | JWT (`ROLE_USER` or `ROLE_ADMIN`) | Current user profile |
| GET | `/api/users/dashboard` | JWT (`ROLE_USER`) | Sample ROLE_USER endpoint |

### Admin endpoints

| Method | Path | Auth required | Description |
|---|---|---|---|
| GET | `/api/admin/dashboard` | JWT (`ROLE_ADMIN`) | Sample ROLE_ADMIN endpoint |
| GET | `/api/admin/users` | JWT (`ROLE_ADMIN`) | Stub — extend with real data |

---

## Example Requests & Responses

### Register

```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "alice@example.com",
  "password": "Secure@123"
}
```

```json
201 Created
{ "message": "Registration successful. Please check your email to confirm your account." }
```

Check MailHog at http://localhost:8025 for the confirmation link.

---

### Confirm email

```
GET /api/auth/confirm?token=<token-from-email>
```

```json
200 OK
{ "message": "Email confirmed. You can now log in." }
```

---

### Login

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "alice@example.com",
  "password": "Secure@123"
}
```

```json
200 OK
{
  "accessToken": "eyJhbGci...",
  "refreshToken": "550e8400-e29b-...",
  "tokenType": "Bearer"
}
```

---

### Get current user

```http
GET /api/users/me
Authorization: Bearer eyJhbGci...
```

```json
200 OK
{
  "id": "d290f1ee-6c54-4b01-90e6-d701748f0851",
  "email": "alice@example.com",
  "enabled": true,
  "roles": ["ROLE_USER"],
  "createdAt": "2024-06-01T10:00:00Z"
}
```

---

### Refresh access token

```http
POST /api/auth/refresh
Content-Type: application/json

{ "refreshToken": "550e8400-e29b-..." }
```

```json
200 OK
{
  "accessToken": "eyJhbGci... (new)",
  "refreshToken": "550e8400-e29b-... (same)",
  "tokenType": "Bearer"
}
```

---

### Change password (authenticated)

```http
POST /api/auth/change-password
Authorization: Bearer eyJhbGci...
Content-Type: application/json

{
  "currentPassword": "Secure@123",
  "newPassword": "NewSecure@456"
}
```

```json
200 OK
{ "message": "Password changed successfully." }
```

---

### Error response shape

All errors return the same JSON structure:

```json
{
  "timestamp": "2024-06-01T10:05:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/auth/register",
  "fieldErrors": [
    { "field": "email", "message": "Must be a valid email address" },
    { "field": "password", "message": "Password must be at least 8 characters" }
  ]
}
```

`fieldErrors` is omitted when there are no field-level validation failures.

---

## Environment Variables

| Variable | Default | Notes |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/trackitdb` | |
| `DB_USERNAME` | `template` | |
| `DB_PASSWORD` | `template` | |
| `JWT_SECRET` | `change-me-to-a-very-long-secret-key-at-least-32-chars!!` | **Change in production. Min 32 chars.** |
| `JWT_ACCESS_TOKEN_EXPIRY_MS` | `900000` | 15 minutes |
| `JWT_REFRESH_TOKEN_EXPIRY_MS` | `604800000` | 7 days |
| `MAIL_HOST` | `smtp.gmail.com` | Dev profile overrides to `localhost` |
| `MAIL_PORT` | `587` | Dev profile overrides to `1025` |
| `MAIL_USERNAME` | _(empty)_ | |
| `MAIL_PASSWORD` | _(empty)_ | |
| `MAIL_FROM` | `noreply@example.com` | |
| `FRONTEND_BASE_URL` | `http://localhost:3000` | Base URL for email links |
| `EMAIL_VERIFICATION_EXPIRY_MS` | `86400000` | 24 hours |
| `PASSWORD_RESET_EXPIRY_MS` | `3600000` | 1 hour |
| `PORT` | `8080` | |

---

## Local Email (MailHog)

MailHog is a local SMTP catcher — no emails actually leave your machine.

- Start: `docker-compose up -d mailhog`
- Web UI: http://localhost:8025 — view all sent emails here
- The `dev` profile wires Spring Mail to MailHog automatically (no config needed)

To swap in a different catcher (e.g. Mailpit), just point `MAIL_HOST` and `MAIL_PORT` at it.

---

## Package Structure

```
com.example.template
├── TemplateApplication.java
├── admin.controller          AdminController          — ROLE_ADMIN sample endpoints
├── auth
│   ├── controller            AuthController           — all /api/auth/* endpoints
│   ├── dto                   request/response DTOs
│   └── service               AuthService              — registration, login, tokens, passwords
├── config
│   ├── AppConfig             @EnableAsync
│   └── DevDataSeeder         seeds admin@example.com (dev profile only)
├── email
│   └── EmailService          async email via JavaMailSender
├── exception
│   ├── ApiException          runtime exception with HTTP status
│   ├── ErrorResponse         uniform JSON error shape
│   ├── FieldError            field-level validation error
│   └── GlobalExceptionHandler  @RestControllerAdvice
├── security
│   ├── JwtService            token generation & validation
│   ├── JwtAuthenticationFilter  per-request JWT extraction
│   ├── UserDetailsServiceImpl   loads User → UserDetails
│   ├── SecurityConfig           filter chain, BCrypt, AuthenticationManager
│   ├── JwtAuthenticationEntryPoint  JSON 401
│   └── JwtAccessDeniedHandler      JSON 403
└── user
    ├── controller            UserController           — /api/users/*
    ├── dto                   UserResponse
    ├── entity                User, Role, *Token entities
    ├── mapper                UserMapper (MapStruct)
    ├── repository            Spring Data JPA repositories
    └── service               UserService
```

---

## Design Decisions

### JWT: access + refresh token split
Short-lived access tokens (15 min) travel in the `Authorization` header for every API call. Long-lived refresh tokens (7 days) are stored server-side in `refresh_tokens` so they can be individually revoked on logout or password change — something pure stateless JWT can't do.

### Roles stored in the JWT
Roles are embedded as a `roles` claim so the authorization filter doesn't need a DB lookup on every request. The trade-off: role changes take up to 15 minutes to propagate (until the current access token expires). Acceptable for most apps; if you need immediate revocation, add a token version/nonce checked against the DB.

### Flyway owns the schema
`ddl-auto: validate` means Hibernate validates entities against the schema but never creates or alters tables. All structural changes go through numbered Flyway migrations, giving you a full audit trail and safe rollback path.

### Email enumeration prevention
`/api/auth/forgot-password` always returns the same success message regardless of whether the email exists in the database. This prevents probing which emails are registered.

### `@Async` email sending
Email is dispatched in a fire-and-forget thread so the HTTP response is returned immediately. The token is persisted before the async call, so by the time email delivery completes (even milliseconds later), the DB row is committed.

### BCrypt cost factor
BCryptPasswordEncoder uses the default cost of 10 — roughly 100 ms/hash on a modern CPU, which is the OWASP minimum recommendation. Increase to 12–13 for higher-value accounts.

---

## Extending for a New Project

1. **Rename the package** — find/replace `com.example.template` → `com.yourcompany.yourapp`
2. **Rename the artifact** in `pom.xml` and `TemplateApplication.java`
3. **Add your domain entities** alongside `user/entity/` and write Flyway migrations for them
4. **Protect new endpoints** with `@PreAuthorize("hasRole('USER')")` (or `ADMIN`) — the filter chain and method security are already wired
5. **Customize email templates** in `EmailService` — swap the inline HTML for Thymeleaf/Freemarker templates when they grow complex
6. **Configure production SMTP** via environment variables — no code changes needed
7. **Change the JWT secret** — set `JWT_SECRET` to the output of `openssl rand -base64 48`
8. **Remove `DevDataSeeder`** or move it behind a `@ConditionalOnProperty` if you don't want dev seeds in staging

---

---

## Order Tracking & To-Do Features

These features are layered on top of the auth scaffold. All endpoints require a valid JWT.

### New environment variables

| Variable | Default | Notes |
|---|---|---|
| `SETTINGS_ENCRYPTION_KEY` | _(empty — transient key used)_ | Base64-encoded 32-byte AES-256 key. Generate: `openssl rand -base64 32`. **Must be set in production** — if blank, encrypted secrets (API key, Gmail tokens) don't survive a restart. |
| `OPENROUTER_API_KEY` | _(empty)_ | Fallback API key for AI email classification. Users can also set their own key per-account via the settings endpoint. |
| `OPENROUTER_DEFAULT_MODEL` | `openrouter/auto` | Model to use when the user hasn't set a preference. |
| `GMAIL_CLIENT_ID` | _(empty)_ | Google OAuth2 client ID for Gmail integration. |
| `GMAIL_CLIENT_SECRET` | _(empty)_ | Google OAuth2 client secret. |
| `GMAIL_REDIRECT_URI` | `http://localhost:8080/api/gmail/callback` | Must match the URI registered in Google Cloud Console. |
| `GMAIL_POLL_INTERVAL_MS` | `300000` | How often Gmail is polled for new emails (5 minutes). |

### Running migrations

Flyway runs automatically on startup. New migrations are V4–V9 and are purely additive — no existing tables are altered.

```bash
# Confirm migrations ran
docker-compose exec postgres psql -U template -d trackitdb -c '\dt'
```

### How to connect Gmail

1. Create a Google Cloud project and enable the **Gmail API**.
2. Create an OAuth2 credential of type **Web Application**.
3. Add `GMAIL_REDIRECT_URI` to the list of authorised redirect URIs.
4. Set `GMAIL_CLIENT_ID` and `GMAIL_CLIENT_SECRET` env vars and restart the backend.
5. In the app, go to **Settings → Connect Gmail** and follow the browser prompt.
6. The app stores your Gmail access/refresh tokens encrypted at rest in `gmail_tokens`.

> **Scope:** `https://www.googleapis.com/auth/gmail.readonly` — the app never reads anything other than email content for order classification.

### Where to put the OpenRouter API key

Two options (user setting takes priority):

1. **Per-user via the app:** Settings screen → paste the key → Save. It is encrypted with AES-256-GCM before storage and never returned in API responses.
2. **Server-wide fallback:** set `OPENROUTER_API_KEY` environment variable.

The model can also be changed per user from the Settings screen (default: `openrouter/auto`).

### How the email → AI → order flow works

```
GmailSyncService (@Scheduled, every 5 min)
  └── for each connected user:
        1. getValidAccessToken (refresh if expired)
        2. fetchNewMessageIds (history API or recent fallback)
        3. for each message:
              fetchMessage → extract subject + plain-text body
              EmailOrderProcessor.process(user, message)
                ├── de-duplicate on gmailMessageId (skip if seen)
                ├── EmailClassificationService.classify(user, emailText)
                │     ├── resolve API key: user settings → env fallback
                │     ├── POST to OpenRouter /chat/completions (JSON mode)
                │     ├── parse + validate EmailClassificationResult
                │     └── retry x2 with backoff on failure
                ├── confidence < 0.5 → create NEEDS_REVIEW order
                ├── intent=new_order → create Order (source=EMAIL)
                ├── intent=status_update → match by externalRef → update status
                └── no match → create NEEDS_REVIEW order
              updateCursor (store latest historyId)
```

If the AI produces malformed JSON, unknown intent, or errors out after retries, the email is stored as a `NEEDS_REVIEW` order for manual inspection — the polling loop never crashes.

### New API endpoints

#### To-Do (`/api/todos`)
| Method | Path | Description |
|---|---|---|
| GET | `/api/todos?sortBy=importance&showDone=false` | List todos (sortBy: createdAt, importance, deadline) |
| POST | `/api/todos` | Create todo |
| GET | `/api/todos/{id}` | Get single todo |
| PUT | `/api/todos/{id}` | Update todo (incl. mark done) |
| DELETE | `/api/todos/{id}` | Delete todo |

#### Orders (`/api/orders`)
| Method | Path | Description |
|---|---|---|
| GET | `/api/orders?status=SHIPPED` | List orders (filter by status) |
| POST | `/api/orders` | Create order manually |
| GET | `/api/orders/{id}` | Get order with full status history |
| PUT | `/api/orders/{id}` | Update order details |
| POST | `/api/orders/{id}/status` | Change status (appends history row) |
| DELETE | `/api/orders/{id}` | Delete order |

#### Settings (`/api/settings`)
| Method | Path | Description |
|---|---|---|
| GET | `/api/settings` | Get settings (key presence flag, model — key never returned) |
| PUT | `/api/settings` | Save OpenRouter API key and/or model |

#### Gmail (`/api/gmail`)
| Method | Path | Description |
|---|---|---|
| POST | `/api/gmail/connect` | Returns `{ authorizationUrl }` — open in browser |
| GET | `/api/gmail/callback` | OAuth2 callback from Google (not JWT-protected) |
| GET | `/api/gmail/status` | `{ connected: true/false }` |
| DELETE | `/api/gmail/disconnect` | Revoke and delete stored tokens |

### Order status lifecycle

```
PENDING → CONFIRMED → SHIPPED → OUT_FOR_DELIVERY → DELIVERED
                   ↘ CANCELLED
                   ↘ RETURNED
                   ↘ NEEDS_REVIEW  (AI flagged for manual review)
```

### Assumptions made

1. Java `RestClient` (Spring Boot 3.2+) is used to call both OpenRouter and Gmail REST APIs directly — no Google Java SDK added, keeping dependencies minimal.
2. User UUID is resolved from email via `UserRepository.findByEmail()` in every new service — same pattern as the existing `AuthService`.
3. Gmail OAuth2 state tokens are held in-memory (10-minute TTL). For multi-instance deployments, replace the `ConcurrentHashMap` in `GmailOAuthController` with a shared cache or DB table.
4. `todos.deadline` is nullable; null deadlines sort last when `sortBy=deadline`.
5. `orders.amount` is `NUMERIC(12,2)` to avoid floating-point drift.
6. Gmail polling runs every 5 minutes (`GMAIL_POLL_INTERVAL_MS=300000`).
7. The first Gmail sync fetches the most recent 50 inbox messages from the last 30 days, then switches to incremental historyId polling.
8. AI classification confidence threshold is `0.5` — emails below this threshold become `NEEDS_REVIEW` orders rather than being silently discarded.
9. Order matching on status-update emails uses `external_ref` (order number) as the primary key. No fuzzy fallback is implemented; unmatched status updates become `NEEDS_REVIEW`.
10. `SETTINGS_ENCRYPTION_KEY` must be exactly 32 bytes when base64-decoded. The app logs a warning and uses a transient key if the variable is unset (dev convenience, not safe for production).

---

## Running Tests

The integration test is disabled by default because it needs PostgreSQL.

```bash
# Start the DB, then run with dev profile
docker-compose up -d
mvn test -Dspring.profiles.active=dev
```

For CI, add [Testcontainers](https://testcontainers.com/):

```xml
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <scope>test</scope>
</dependency>
```

Then annotate your test with `@Testcontainers` + `@Container PostgreSQLContainer` and remove `@Disabled`.
