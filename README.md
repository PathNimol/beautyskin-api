# BS Online Shop API

Spring Boot REST backend for the [BS Online Shop](../) Next.js frontend.

## Tech stack

- Java 21
- Spring Boot 3.4
- Spring Data JPA + PostgreSQL
- Spring Security + JWT (access + refresh tokens)
- MapStruct + Lombok
- SpringDoc OpenAPI (Swagger UI)
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

Base URL: `http://localhost:8080/api`

## Run locally

```bash
cd backend
gradle wrapper   # first time only
./gradlew bootRun        # Linux/macOS
gradlew.bat bootRun      # Windows
```

On first startup, demo data is seeded automatically.

### Demo accounts (match UI)

| Email | Password | Role |
|-------|----------|------|
| admin@beautyskin.com | admin123 | ADMIN |
| owner@beautyskin.com | owner123 | OWNER |
| staff@beautyskin.com | staff123 | STAFF |
| buyer@beautyskin.com | buyer123 | CUSTOMER |

OTP demo code (register / forgot password): `123456`

## API documentation

- Swagger UI: [http://localhost:8080/api/swagger-ui.html](http://localhost:8080/api/swagger-ui.html)
- OpenAPI JSON: [http://localhost:8080/api/v3/api-docs](http://localhost:8080/api/v3/api-docs)

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
3. Refresh via `POST /api/auth/refresh` with `{ "refreshToken" }`

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
