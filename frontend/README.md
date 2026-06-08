# React Native Auth Template (Expo)

Expo SDK 54 · React Native 0.81.5 · React 19 · TypeScript · Expo Router v6

A production-ready authentication starter that runs on **Android, iOS, and Web** from a single codebase. Pair it with the Spring Boot backend in `../backend/`.

## Features

- Register with email confirmation flow
- Login / logout with JWT access + refresh tokens
- Forgot password / reset password via deep link
- Change password (authenticated)
- Auto token refresh: silent 401 retry, forced logout on refresh failure
- Secure token storage: Keychain/Keystore on native, localStorage on web
- Adaptive layout: full-width on mobile, centered card on web
- Light / dark mode support
- Typed API client with centralized error handling

---

## Quick Start

### 1. Install dependencies

```bash
cd frontend
npm install
```

### 2. Configure the API base URL

```bash
cp .env.example .env
```

Edit `.env`:

```
EXPO_PUBLIC_API_URL=http://localhost:8080
```

On **Android emulator**, replace `localhost` with `10.0.2.2`:

```
EXPO_PUBLIC_API_URL=http://10.0.2.2:8080
```

### 3. Start the backend

```bash
cd ../backend
docker-compose up -d
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### 4. Run the app

```bash
# Web (browser at http://localhost:8081)
npm run web

# Android emulator / device
npm run android

# iOS simulator (macOS only)
npm run ios
```

---

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `EXPO_PUBLIC_API_URL` | `http://localhost:8080` | Spring Boot backend base URL |

All `EXPO_PUBLIC_*` variables are baked into the JS bundle at build time. Add new ones with the same prefix and reference them as `process.env.EXPO_PUBLIC_*`.

---

## Deep Linking

Email confirmation and password-reset emails contain links. Those links must open the correct screen on **web** and deep-link into the **native app**.

### URL patterns

| Flow | Web URL | Native URL |
|---|---|---|
| Email confirmation | `http://your-domain/confirm?token=…` | `templateapp://confirm?token=…` |
| Password reset | `http://your-domain/reset-password?token=…` | `templateapp://reset-password?token=…` |

> **Note:** The `(auth)` folder in `app/(auth)/` is an Expo Router layout group — the parentheses are transparent in the URL. `app/(auth)/confirm.tsx` maps to `/confirm`, not `/auth/confirm`.

### Backend configuration

Set this env var on the Spring Boot backend so it generates the correct URLs in emails:

```
FRONTEND_BASE_URL=http://localhost:3000   # development (web)
```

For **production**, set it to your deployed web domain. The native app will intercept the same URL via [Universal Links (iOS)](https://docs.expo.dev/guides/linking/#universal-links-on-ios) / [App Links (Android)](https://docs.expo.dev/guides/linking/#app-links-on-android) once you configure the `.well-known` files on your server.

### Changing the app scheme

The current scheme is `templateapp` (set in `app.json` → `expo.scheme`). To rename it:

1. Update `expo.scheme` in `app.json`
2. Update the table above in this README
3. Rebuild the native app (`npx expo prebuild --clean`)

---

## Project Structure

```
frontend/
├── app/                         Expo Router file-based routes
│   ├── _layout.tsx              Root layout: AuthProvider + SafeAreaProvider
│   ├── index.tsx                Entry — redirects to login or home
│   ├── (auth)/                  Public screens (no auth required)
│   │   ├── _layout.tsx
│   │   ├── login.tsx
│   │   ├── register.tsx
│   │   ├── forgot-password.tsx
│   │   ├── reset-password.tsx   Reads ?token= from URL / deep link
│   │   └── confirm.tsx          Reads ?token= from URL / deep link
│   └── (app)/                   Protected screens (auth required)
│       ├── _layout.tsx          Redirects to login if unauthenticated
│       ├── home.tsx             First screen after login
│       └── change-password.tsx
├── components/
│   ├── Button.tsx               Primary / ghost / danger variants, loading state
│   ├── Card.tsx                 Elevated surface container
│   ├── FormField.tsx            Label + TextInput + error message
│   ├── Screen.tsx               Safe-area wrapper with keyboard avoidance
│   └── TextInput.tsx            Focusable input with error border + message
├── contexts/
│   └── AuthContext.tsx          Auth state, login / logout / register
├── lib/
│   ├── api/
│   │   ├── client.ts            fetch wrapper: auth headers, 401 refresh, error shape
│   │   └── endpoints.ts         Typed functions for each backend endpoint
│   └── storage.ts               Token persistence (SecureStore / localStorage)
├── theme/
│   └── index.ts                 Colors, spacing, typography, radius, dark mode hook
└── types/
    └── api.ts                   Request / response TypeScript types
```

---

## Auth Flow

```
App start
  └─ Restore tokens from storage
       ├─ No tokens → login screen
       └─ Tokens found → GET /api/users/me
            ├─ OK → mark authenticated, show home
            └─ Fail → clear tokens, show login

Login
  └─ POST /api/auth/login → store tokens → GET /api/users/me → show home

API request (authenticated)
  └─ 401 received → POST /api/auth/refresh
       ├─ OK → retry original request
       └─ Fail → clear tokens → force logout → show login

Logout
  └─ POST /api/auth/logout (server-side revoke) → clear tokens → show login
```

---

## Extending for a New Project

1. **Rename the app** — update `name`, `slug`, `scheme`, `bundleIdentifier`, and `package` in `app.json`
2. **Add screens** — drop files in `app/(app)/` for protected routes, `app/(auth)/` for public ones
3. **Extend user types** — add fields to `UserResponse` in `types/api.ts` and update `AuthContext` if needed
4. **Customize the theme** — edit `palette` in `theme/index.ts` to match your brand
5. **Add app icons** — place a 1024×1024 `icon.png` and a splash image in `assets/`, then reference them in `app.json`
6. **Configure production SMTP** on the backend via environment variables — no frontend changes needed

---

## Assets

The app references `assets/icon.png` and `assets/splash.png` in `app.json`. You must add these before building a native binary:

- `icon.png` — 1024 × 1024 px
- `splash.png` — 1242 × 2436 px (or configure `expo-splash-screen` image resizing)

For development on web, missing assets do not prevent the app from running.

---

## Design System

| Token | Location | Description |
|---|---|---|
| Colors | `theme/index.ts` → `lightColors` / `darkColors` | Semantic color tokens |
| Spacing | `theme/index.ts` → `spacing` | `xs` (4) through `xxl` (48) |
| Typography | `theme/index.ts` → `typography` | Font sizes and weights |
| Radius | `theme/index.ts` → `radius` | Border radius scale |
| Dark mode | `useThemeColors()` hook | Returns correct color set automatically |

Components use semantic colors from `useThemeColors()`, so dark mode is handled globally.
