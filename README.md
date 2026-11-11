# DecisionHub

DecisionHub is a collaborative decision-making platform. Users can create boards with choices, collect votes and comments, and use community feedback to make decisions.

> **Project status:** The Spring Boot backend is implemented. A frontend application is not currently present in this repository; the `frontend/` directory is reserved for it.

## How it fits together

The backend is a REST API. A future or external web client can send HTTP requests to it; the API validates the request, applies application rules, and reads or writes data in MySQL.

```text
Web client (not included yet)
        |
        | HTTP / JSON, optionally with a JWT
        v
Spring Boot REST API (backend/)
  Controller -> Service -> Repository
        |
        v
Database (MySQL; H2 for development)
```

- **Controllers** expose HTTP endpoints and translate requests and responses.
- **Services** implement application rules, such as board ownership and voting.
- **Repositories** load and save data through Spring Data JPA.
- **Entities** represent persisted records, such as users, boards, options, votes, and comments.
- **DTOs** define the data exchanged through the API.
- **Security** handles password-based authentication, JWTs, and optional Google OAuth2 login.

## Main capabilities

- Register and sign in with email and password; authenticated requests use JWTs.
- Create and manage public or private decision boards and their options.
- Vote on boards and discuss them with comments.
- Create and join communities.
- View notifications, profile information, and dashboard analytics.

Private boards are currently owner-only; invitations and several planned integrations are not implemented. See the [backend guide](./backend/README.md) for the API route list, configuration details, and current limitations.

## Repository layout

```text
DecisionHub/
  backend/
    pom.xml                  Maven dependencies and Java version
    Dockerfile               Container build for the API
    src/main/java/com/decisionhub/
      controller/            REST endpoints
      service/               Business rules
      repository/            Database access
      entity/                Database entities
      dto/                   Request/response data types
      config/                Spring configuration
      security/              Authentication and authorization
    src/main/resources/      Application configuration
    src/test/                Automated tests
  database/                  Reserved for database-related assets
  docs/                      Project documentation
  frontend/                  Reserved for a web client
  compose.yaml               Local MySQL and backend services
```

## Run locally

The backend requires **Java 25 (LTS)** and **Maven 3.9 or later**.

### Quick development run

This uses an in-memory H2 database. Data is discarded when the application stops:

```powershell
cd backend
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
```

### Run tests

From the `backend/` directory:

```powershell
mvn test
```

### Run with MySQL using Docker Compose

From the repository root, set a private JWT secret (at least 32 bytes) before starting the services:

```powershell
$env:JWT_SECRET = "<private random value of at least 32 bytes>"
docker compose up --build
```

The API is then available at `http://localhost:8080`. For database settings, environment variables, OAuth2 configuration, and troubleshooting, follow the [backend setup guide](./backend/README.md).

## Authentication in a nutshell

1. Register or log in using the authentication endpoints listed in the [backend API overview](./backend/README.md).
2. For protected endpoints, send an `Authorization` header with the value `Bearer` followed by the JWT returned at login.
3. Keep the token private. The backend uses it to identify the signed-in user.

## Further reading

- [API guide with example requests and responses](./docs/API-GUIDE.md)
- [Backend setup, API overview, configuration, and limitations](./backend/README.md)
