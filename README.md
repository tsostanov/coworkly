# Coworkly

[![CI](https://github.com/tsostanov/coworkly/actions/workflows/ci.yml/badge.svg?branch=master&event=push)](https://github.com/tsostanov/coworkly/actions/workflows/ci.yml)

Coworkly is a full-stack pet project for coworking space management.

The repository contains a Spring Boot backend, a React frontend, database artifacts, and supporting coursework files. The main runnable application lives in the nested `coworkly/` directory.

## What The Project Does

Coworkly models a small coworking platform with:

- user registration and login
- JWT-based authentication and role-based access
- locations and workspaces
- resident bookings
- admin walk-in bookings
- visit check-in / checkout / extension flows
- penalties
- admin reports

The backend exposes REST APIs for both resident and admin scenarios. The frontend is a separate React application that works against those APIs.

## Main Stack

### Backend
- Java 17
- Spring Boot 3
- Spring Web
- Spring Data JPA
- Spring Security
- Bean Validation
- PostgreSQL
- Flyway
- JUnit 5 / MockMvc / H2 for tests

### Frontend
- React
- TypeScript
- Vite

## Repository Layout

```text
.
|-- coworkly/     # main runnable application
|   |-- src/      # Spring Boot backend
|   |-- frontend/ # React frontend
|   |-- pom.xml
|   `-- README.md
|-- stage2/       # SQL schema, triggers, seed data, ERD artifacts
`-- src/          # additional source artifacts kept at repository root
```

## Important Note

If you want to run, build, or test the actual application, work inside:

```bash
cd coworkly
```

The root of the repository is mostly an umbrella for the full coursework/project history. The active Maven module and frontend app are inside `coworkly/`.

## Implemented API Areas

From the current backend structure, the main API areas are:

- `/api/auth`
- `/api/locations`
- `/api/spaces`
- `/api/bookings`
- `/api/penalties`
- `/api/admin/users`
- `/api/admin/locations`
- `/api/admin/spaces`
- `/api/admin/penalties`
- `/api/admin/walkin`
- `/api/admin/visits`
- `/api/admin/reports`

## Quick Start

### Requirements

- JDK 17+
- Maven 3.9+ or Maven Wrapper
- Node.js 18+
- npm 9+
- PostgreSQL 14+

### Run backend

```bash
cd coworkly
./mvnw spring-boot:run
```

By default, the backend runs on `http://localhost:8081`.

### OpenAPI and Swagger UI

After the backend starts, API documentation is available at:

- `http://localhost:8081/swagger-ui/index.html`
- `http://localhost:8081/v3/api-docs`

### Run frontend

```bash
cd coworkly/frontend
npm install
npm run dev
```

By default, the frontend runs on `http://localhost:5173`.

## Build

### Backend

```bash
cd coworkly
./mvnw clean package
```

### Frontend

```bash
cd coworkly/frontend
npm run build
```

## Testing

Backend tests are already configured in the main module:

```bash
cd coworkly
./mvnw test
```

The current test strategy includes:

- unit tests for service-level business rules
- API tests with `MockMvc`
- a Spring Boot smoke test

The test profile uses an in-memory H2 database, so test execution does not require a local PostgreSQL instance.

## CI

The repository includes a GitHub Actions workflow that runs on every push to `master` and on pull requests.

It currently checks:

- backend tests with Maven
- frontend type checking
- frontend production build

## Database Artifacts

The `stage2/` directory contains SQL and modeling materials, including:

- schema scripts
- triggers and functions
- indexes
- seed data
- ERD-related files

## Notes

- The active application module has its own module-level README in [coworkly/README.md](./coworkly/README.md).
- The current backend configuration in `coworkly/src/main/resources/application.properties` should be externalized before any production-like deployment.
