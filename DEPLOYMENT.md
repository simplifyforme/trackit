# Deployment Tutorial

Complete guide to deploy a Spring Boot + Expo app with email sending, from source code to production.

---

## Overview

| What | Where |
|---|---|
| Backend (Spring Boot) | Render (Docker) |
| Database (PostgreSQL) | Render |
| Email sending | Resend |
| Sender domain | Namecheap |
| Frontend (Expo) | Not covered — deploy separately (Vercel, EAS, etc.) |

---

## Part 1 — Protect secrets before pushing to GitHub

### 1.1 Never hardcode credentials

In `application.yml`, always use environment variable syntax:

```yaml
spring:
  mail:
    username: ${MAIL_USERNAME}   # no hardcoded value
    password: ${MAIL_PASSWORD}
```

### 1.2 Create a root `.gitignore`

At the root of your project, create `.gitignore`:

```gitignore
# Secrets
.env
.env.local
*.env
application-local.yml
application-local.yaml

# Java build output
backend/target/

# Node / Expo
frontend/node_modules/
frontend/.expo/
frontend/.env
```

### 1.3 Create `backend/.env` for local secrets (never committed)

```env
DB_URL=jdbc:postgresql://localhost:5432/yourdb
DB_USERNAME=youruser
DB_PASSWORD=yourpassword

RESEND_API_KEY=your_resend_api_key
MAIL_FROM=noreply@yourdomain.com

JWT_SECRET=a-very-long-random-string-at-least-32-chars
FRONTEND_BASE_URL=http://localhost:8081
```

### 1.4 Create `backend/application-local.yml` for local Spring Boot overrides (never committed)

```yaml
app:
  resend:
    api-key: your_resend_api_key
  jwt:
    secret: a-very-long-random-string-at-least-32-chars
  frontend-base-url: http://localhost:8081
  email:
    from: noreply@yourdomain.com
```

Run locally with:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### 1.5 Create `backend/.env.example` (this one IS committed — it's a template)

```env
DB_URL=jdbc:postgresql://localhost:5432/yourdb
DB_USERNAME=youruser
DB_PASSWORD=yourpassword

RESEND_API_KEY=
MAIL_FROM=noreply@yourdomain.com

JWT_SECRET=replace-with-long-random-string
FRONTEND_BASE_URL=http://localhost:8081
```

---

## Part 2 — Switch email sending to Resend

Render blocks all outbound SMTP ports (25, 465, 587) on all plans. Resend sends email over HTTPS (port 443) which is never blocked.

### 2.1 Add Resend dependency to `pom.xml`

```xml
<dependency>
    <groupId>com.resend</groupId>
    <artifactId>resend-java</artifactId>
    <version>3.1.0</version>
</dependency>
```

### 2.2 Add Resend config to `application.yml`

```yaml
app:
  resend:
    api-key: ${RESEND_API_KEY:}
  email:
    from: ${MAIL_FROM:noreply@yourdomain.com}
```

### 2.3 Rewrite `EmailService.java`

```java
@Service
@Slf4j
public class EmailService {

    private final Resend resend;

    @Value("${app.email.from}")
    private String fromAddress;

    @Value("${app.frontend-base-url}")
    private String frontendBaseUrl;

    public EmailService(@Value("${app.resend.api-key}") String apiKey) {
        this.resend = new Resend(apiKey);
    }

    @Async
    public void sendVerificationEmail(String to, String token) {
        String link = frontendBaseUrl + "/confirm?token=" + token;
        send(to, "Confirm your email address", "<html><body>..."
            .formatted(link));
    }

    private void send(String to, String subject, String html) {
        try {
            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(fromAddress)
                    .to(to)
                    .subject(subject)
                    .html(html)
                    .build();
            resend.emails().send(params);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
```

---

## Part 3 — Buy a domain on Namecheap

1. Go to **namecheap.com** → search for a domain (e.g. `yourapp.website`)
2. Add to cart → checkout
3. Your domain appears in **Dashboard → Domain List**

---

## Part 4 — Set up Resend

### 4.1 Create account and get API key

1. Sign up at **resend.com**
2. Go to **API Keys → Create API Key** → copy it

### 4.2 Add and verify your domain

1. Resend → **Domains → Add Domain**
2. Enter your domain (e.g. `yourapp.website`) → click **Add**
3. Resend shows you a table with DNS records to add:

| Type | Host | Value |
|---|---|---|
| TXT | `resend._domainkey` | `p=MIGf...` (DKIM key) |
| MX | `send` | `feedback-smtp.us-east-1.amazonses.com` |
| TXT | `send` | `v=spf1 include:amazonses.com ~all` |

### 4.3 Add DNS records on Namecheap

1. Namecheap → **Domain List → Manage** → **Advanced DNS** tab
2. Click **Add New Record** for each record from Resend
3. For the **Host** field: if Resend says `send.yourapp.website`, type only `send` (Namecheap adds the domain automatically)
4. MX Record type is at the bottom of the type dropdown — scroll down to find it

> The MX record goes in the **main DNS table at the top**, NOT in the "Mail Settings" section at the bottom.

### 4.4 Verify

- Go back to Resend → Domains → click **Verify DNS Records**
- Wait 5–30 minutes for DNS to propagate
- All records will show green checkmarks ✅
- Status changes to **Verified**

---

## Part 5 — Deploy backend to Render

### 5.1 Create a Dockerfile in `backend/`

```dockerfile
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn clean package -DskipTests -q

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 5.2 Create PostgreSQL database on Render

1. Render → **New → PostgreSQL**
2. Give it a name → click **Create Database**
3. On the database page, copy the **Internal Database URL** (looks like `postgresql://user:pass@host/dbname`)

### 5.3 Create Web Service on Render

1. Render → **New → Web Service**
2. Connect your GitHub repository
3. Fill in the settings:

| Field | Value |
|---|---|
| **Root Directory** | `backend` |
| **Runtime** | `Docker` |
| **Build Command** | *(leave empty)* |
| **Start Command** | *(leave empty)* |

### 5.4 Set environment variables on Render

Go to your service → **Environment** tab → add these variables:

| Variable | Value |
|---|---|
| `DB_URL` | Internal Database URL from step 5.2 (change `postgresql://` to `jdbc:postgresql://`) |
| `DB_USERNAME` | From Render database page |
| `DB_PASSWORD` | From Render database page |
| `RESEND_API_KEY` | From Resend → API Keys (must be from same account as verified domain) |
| `MAIL_FROM` | `noreply@yourdomain.com` |
| `JWT_SECRET` | Run `openssl rand -base64 48` to generate |
| `FRONTEND_BASE_URL` | Your deployed frontend URL (or backend URL temporarily) |

> Do NOT add `PORT` — Render injects it automatically.

### 5.5 Deploy

Click **Deploy**. Render builds the Docker image and starts the app.
Check the **Logs** tab to confirm it started successfully.

---

## Part 6 — Connect frontend to deployed backend

In `frontend/.env`:

```env
# Local development
EXPO_PUBLIC_API_URL=http://localhost:8080

# Switch to this when testing against Render
# EXPO_PUBLIC_API_URL=https://your-app.onrender.com
```

> When running Expo on a **physical device**, `localhost` won't work.
> Use your PC's local IP instead: `http://192.168.1.X:8080`
> Find it with `ipconfig` (Windows) or `ifconfig` (Mac/Linux).

---

## Common issues

| Problem | Cause | Fix |
|---|---|---|
| Emails not sending on Render | SMTP ports blocked | Use Resend (this guide) |
| Resend returns 403 "domain not verified" | API key from different account than verified domain | Generate new API key from same Resend account where domain is verified |
| Resend returns 403 after env var update | Render still running old version | Trigger manual redeploy on Render |
| `MAIL_FROM` using wrong domain | `.com` vs `.website` mismatch | Make sure `MAIL_FROM` exactly matches the domain verified on Resend |
| Confirmation link points to localhost | `FRONTEND_BASE_URL` not set on Render | Add `FRONTEND_BASE_URL` to Render environment variables |
