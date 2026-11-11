# DecisionHub backend

Spring Boot REST API for accounts, decision boards, polling, discussions, communities, notifications, and the user dashboard.

## Requirements

- Java 25 (LTS)
- Maven 3.9+
- MySQL 8+ (or configure another Spring Data JPA-compatible database)

Run with the default MySQL configuration from this directory:

```powershell
mvn spring-boot:run
```

For a quick local run without MySQL, use the `dev` profile. It uses an in-memory H2 database, so its data is discarded when the process stops:

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
```

To run the backend with MySQL using Docker Compose from the repository root, set a private JWT secret and start both services:

```powershell
$env:JWT_SECRET = "<private random value of at least 32 bytes>"
docker compose up --build
```

By default the API uses `jdbc:mysql://localhost:3306/decisionhub`, database user `root`, and an empty password. Set `DATABASE_URL`, `DATABASE_USERNAME`, and `DATABASE_PASSWORD` to override those settings. Hibernate uses `ddl-auto=update` for local development; use a migration tool and set `DDL_AUTO=validate` in production.

Set `JWT_SECRET` to a private, random secret of at least 32 bytes before deploying. `JWT_EXPIRATION_MS` defaults to 24 hours. Set `CORS_ALLOWED_ORIGINS` to a comma-separated list of trusted frontend origins.

### OAuth2

Google OAuth2 login is enabled when Spring Security has a client registration. Configure `spring.security.oauth2.client.registration.google.client-id`, `client-secret`, and `scope` using environment-backed Spring properties (for example `SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_ID` and `..._CLIENT_SECRET`). The callback URL is `/login/oauth2/code/google`; successful login returns a DecisionHub JWT in a JSON response. OAuth2 is optional; email/password JWT authentication works without a provider.

## API overview

See the [API guide](../docs/API-GUIDE.md) for example requests and responses for authentication, boards, votes, and comments.

All routes are prefixed with `/api`. Send `Authorization: Bearer <token>` for authenticated routes.

| Area | Routes |
|---|---|
| Authentication | `POST /auth/register`, `POST /auth/login` |
| Profile | `GET /users/me`, `PUT /users/me`, `PUT /users/me/password` |
| Boards | `GET /boards/public`, `GET /boards/mine`, `POST /boards`, `GET /boards/{id}`, `PUT /boards/{id}`, `DELETE /boards/{id}`, `POST /boards/{id}/options` |
| Voting | `POST /boards/{id}/votes`, `GET /boards/{id}/votes` |
| Discussion | `GET /boards/{id}/comments`, `POST /boards/{id}/comments` |
| Communities | `GET /communities`, `POST /communities`, `POST /communities/{id}/members`, `DELETE /communities/{id}/members/me`, `GET /communities/{id}/members`, `DELETE /communities/{id}` |
| Notifications | `GET /notifications`, `PATCH /notifications/{id}/read` |
| Analytics | `GET /analytics/dashboard` |

Board creation requires at least two options. Visibility is `PUBLIC` or `PRIVATE`; poll type is `SINGLE_CHOICE`, `MULTIPLE_CHOICE`, or `RATING`. Anonymous votes do not associate a user with the vote. Private boards are accessible only to their owners in this initial backend; invite-based private sharing is not implemented yet.

Password updates require the current password. Email-based password recovery, invited private-board access, report exports, and scheduled/reminder notifications need additional mail, sharing, and reporting integrations and are not part of this initial backend implementation.

Community categories are `CAREER`, `EDUCATION`, `TECHNOLOGY`, `TRAVEL`, `FINANCE`, `LIFESTYLE`, and `OTHER`. Account roles are `USER`, `MODERATOR`, and `ADMIN`; new accounts receive `USER`. Promote administrative accounts through a controlled database/admin process, never from public registration.

## Tests

```powershell
mvn test
```

Tests use an in-memory H2 database and do not require a running MySQL server.
