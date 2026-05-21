# BS Online Shop API

Spring Boot REST backend for the [BS Online Shop](../) Next.js frontend.

## Tech stack

- Java 21
- Spring Boot 3.4
- Spring Data JPA + PostgreSQL + **Flyway** (versioned migrations; JPA `ddl-auto` still syncs model during transition)
- Spring Security + JWT (access + refresh tokens)
- MapStruct + Lombok
- SpringDoc OpenAPI (Swagger UI)
- Spring Mail (optional SMTP for OTP emails)
- Gradle

## Prerequisites

- JDK 21
- PostgreSQL 14+
- Gradle 8.x (or use the Gradle wrapper after generating it)

## Database setup

```sql
CREATE DATABASE beautyskin-db;
CREATE USER bsonlineshop WITH ENCRYPTED PASSWORD 'bsonlineshop';
GRANT ALL PRIVILEGES ON DATABASE bsonlineshop TO bsonlineshop;
```

## Migrations (Flyway)

- Scripts live in `src/main/resources/db/migration/`.
- **`V1__flyway_startup_marker.sql`** records the first Flyway revision; Hibernate still applies **`ddl-auto: update`** so the live schema follows entities.
- **Next step for production hardening:** freeze schema in Flyway (baseline SQL for all tables + set `ddl-auto: validate` / `none`).
- Tests use **`spring.flyway.enabled=false`** and an in-memory H2 schema via `create-drop`.

## Configuration

Environment variables (optional overrides):

| Variable | Default                 | Description |
|----------|-------------------------|-------------|
| `DB_HOST` | `localhost`             | PostgreSQL host |
| `DB_PORT` | `5432`                  | PostgreSQL port |
| `DB_NAME` | `beautyskin-db`         | Database name |
| `DB_USER` | `postgres`              | Database user |
| `DB_PASSWORD` | `151003`                | Database password |
| `SERVER_PORT` | `8080`                  | HTTP port |
| `JWT_SECRET` | (see `application.yml`) | JWT signing secret (min 32 chars) |
| `CORS_ORIGINS` | `http://localhost:4028` | Allowed frontend origins |
| `SPRING_MAIL_HOST` | _unset_ | SMTP host (e.g. `smtp.gmail.com`). If unset, OTP is stored only—no email is sent. |
| `SPRING_MAIL_PORT` | `587` | SMTP port |
| `SPRING_MAIL_USERNAME` | _unset_ | SMTP auth username (often same as From for Gmail) |
| `SPRING_MAIL_PASSWORD` | _unset_ | SMTP password (use an [App Password](https://support.google.com/accounts/answer/185833) for Gmail) |
| `APP_MAIL_FROM` | _spring.mail.username_ | Visible From address (required if username is not an email) |
| `APP_OAUTH_DEMO_STUB_ENABLED` | `true` | When `false`, `POST /api/auth/oauth/{provider}` is rejected (use real OAuth2 or browser flow). |
| `SPRING_PROFILES_ACTIVE` | _unset_ | Set to `prod` for stricter registration + no demo OTP stub (see `application-prod.yml`). |

Base URL: `http://localhost:8080/api`

## Run locally

```bash
cd beautyskin-api
./gradlew bootRun        # Linux/macOS/Git Bash — use gradlew.bat on cmd.exe
```

Activate **`prod`** with `-Dspring-boot.run.profiles=prod` or `SPRING_PROFILES_ACTIVE=prod`.

### Production profile (`prod`)

`src/main/resources/application-prod.yml` turns on **`app.auth.registration.require-delivered-email=true`** (SMTP must send the registration OTP or the transaction rolls back), clears **`app.otp.demo-code`**, and disables the demo OAuth stub.

## Security & roles

- **CUSTOMER** storefront: cart, checkout, place order, submit reviews (ADMIN also allowed where noted for support flows).
- **OWNER / STAFF** + **ADMIN**: merchant backends (inventory, POS, promotions, suppliers, analytics summary, shop dashboard, order status changes, etc.). Shop-scoped handlers use `User.shopId`; **ADMIN** bypasses shop checks where supported.
- **`AuthorizationExpressions` (`@authz`)** backs `@PreAuthorize("@authz...")` checks. This is the extension point for finer DB-backed permissions later.

On first startup (empty database — **no users yet**), demo accounts, shops (**glowskin**, **kbeauty**), promotions, chat rooms, and catalog products are seeded once.

**Catalog:** Showcase SKUs **`UI-SHOWCASE-001` … `UI-SHOWCASE-008`** mirror the Next.js home “Best Sellers”; extra browse rows use **`CAT-EXTRA-*`**. Catalog seed runs **only** with that initial demo seed, not on subsequent startups.

### Demo accounts (match UI)

| Email | Password | Role |
|-------|----------|------|
| admin@beautyskin.com | admin123 | ADMIN |
| owner@beautyskin.com | owner123 | OWNER |
| staff@beautyskin.com | staff123 | STAFF |
| buyer@beautyskin.com | buyer123 | CUSTOMER |

### Customer registration (email verification)

1. **`POST /api/auth/register`** creates a **PENDING_EMAIL_VERIFICATION** customer (no JWT). An OTP with purpose **`REGISTER_EMAIL`** is emailed when SMTP is configured, or stored in the DB in local dev.
2. **`POST /api/auth/register/confirm`** with `{ "email", "code" }` validates the OTP, sets **ACTIVE** + **`emailVerified=true`**, returns tokens.
3. Do **not** use **`POST /api/auth/otp/verify`** for `REGISTER_EMAIL` (it is rejected).

### Demo OTP

When **`app.otp.demo-code`** is non-empty (default **`123456`** in dev), it still satisfies OTP checks while a matching row exists. **Production (`prod`)** ships with an empty value so only real codes apply.

OTP codes are sent by email when **`SPRING_MAIL_HOST`** (and auth credentials) are set. **`POST /api/auth/password/forgot`** only persists an OTP when an **ACTIVE** account exists for that email (silent otherwise). **`POST /api/auth/otp/send`** with **`REGISTER_EMAIL`** only works for pending registrations.

## API documentation

- Swagger UI: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- OpenAPI JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

**Swagger bearer token:** Click **Authorize** → `bearerAuth` → paste the JWT from `POST /api/auth/login` (value only, no `Bearer ` prefix). With `springdoc.swagger-ui.persist-authorization=true`, the token is kept in the browser after refresh until you clear site data or click **Logout** in Authorize.

## Response format

All endpoints return:

```json
{
  "status": "success",
  "message": "OK",
  "data": {},
  "timestamp": "2026-05-18T10:00:00Z"
}
```

Paginated lists wrap items in `data.content` with `page`, `size`, `totalElements`, `totalPages`.

## Authentication

1. `POST /api/auth/login` with `{ "email", "password" }`
2. Use `Authorization: Bearer <accessToken>` on protected routes
3. Refresh via `POST /api/auth/refresh` with `{ "refreshToken" }` (rotates refresh token; old one is revoked)
4. `POST /api/auth/logout` with `{ "refreshToken" }` to sign out one session
5. `POST /api/auth/logout-all` with Bearer token to revoke all refresh tokens for that user

**Register:** public signup always creates a `CUSTOMER`. Password: 8–128 chars, at least one letter and one number; `confirmPassword` must match.

**Account status:** only `ACTIVE` users can log in or use tokens. `INACTIVE` / `SUSPENDED` accounts are rejected.

## UI → API mapping (summary)

| UI area | Endpoints |
|---------|-----------|
| Login / Register / OTP | `/auth/*` |
| Account / shipping | `/users/me`, `/users/me/shipping` |
| Product listing / PDP | `GET /products`, `GET /products/{id}`, reviews |
| Cart / promo codes | `/cart`, `/cart/promo` |
| Checkout | `GET /checkout/quote`, `POST /orders` |
| Admin / owner dashboard | `/admin/dashboard`, `/dashboard?shopId=` |
| Shops management | `/shops` |
| Shop users | `/shops/{shopId}/users` |
| Merchant products | `/products/merchant`, `/products/shops/{shopId}` |
| Orders | `/orders`, bulk status patch |
| Inventory | `/inventory`, restock |
| Promotions | `/promotions` |
| Suppliers | `/suppliers` |
| POS | `/pos/sales`, `/pos/receipts` |
| Revoke requests | `/revoke-requests` |
| Customers | `/customers` |
| Chat / DMs | `/chat/*`, `/messages/*` |
| Featured / categories | `/catalog/*` |

## Project structure

```
src/main/java/com/acleda/bsonlineshop/
├── config/          # Security, CORS, OpenAPI, JPA auditing
├── controller/      # REST controllers
├── dto/             # Request/response DTOs
├── entity/          # JPA entities
├── enums/
├── exception/       # @ControllerAdvice
├── mapper/          # MapStruct + manual mappers
├── repository/
├── security/        # JWT filter, UserPrincipal
├── seed/            # Demo data seeder
└── service/         # Interfaces
    └── impl/        # Service implementations
```

## Connect frontend

Point the Next.js app to `http://localhost:8080/api` and replace mock/Supabase calls with fetch/axios to these endpoints. Send JWT in the `Authorization` header and store tokens from `/auth/login` or `/auth/register`.

## Build & test

```bash
./gradlew build
./gradlew test
```
