# Coworkly

[![CI](https://github.com/tsostanov/coworkly/actions/workflows/ci.yml/badge.svg)](https://github.com/tsostanov/coworkly/actions/workflows/ci.yml)

Coworkly is a full-stack pet project for coworking space management. It includes a Spring Boot backend with REST APIs and a React + TypeScript frontend.

## Tech Stack

### Backend
- Java 17
- Spring Boot 3
- Spring Web / Data JPA / Validation / Security
- Flyway
- PostgreSQL
- JWT (`jjwt`)
- Maven

### Frontend
- React 18
- TypeScript
- Vite

## Project Structure

```text
coworkly/
|-- src/                     # backend (Spring Boot)
|-- frontend/                # frontend (React + Vite)
|-- pom.xml                  # Maven config for backend
`-- README.md
```

## Quick Start

### Requirements
- JDK 17+
- Maven 3.9+ or `./mvnw`
- Node.js 18+
- npm 9+
- PostgreSQL 14+

### Run backend

From the project root:

```bash
./mvnw spring-boot:run
```

Backend starts on `http://localhost:8081` by default.

### OpenAPI and Swagger UI

After the backend starts, interactive API documentation is available at:

- `http://localhost:8081/swagger-ui/index.html`
- `http://localhost:8081/v3/api-docs`

### Run frontend

In a separate terminal:

```bash
cd frontend
npm install
npm run dev
```

Frontend starts on `http://localhost:5173` by default.

## Build

### Backend

```bash
./mvnw clean package
```

### Frontend

```bash
cd frontend
npm run build
```

## Useful Commands

### TypeScript check

```bash
cd frontend
npm run lint
```

### Generate PlantUML class diagram

```bash
./mvnw verify
```

The generated diagram is written to `target/uml/classes.puml`.

## Testing

Run the backend test suite with:

```bash
./mvnw test
```

The current backend testing strategy is split into three layers:

- Unit tests for service-level business rules.
- API tests with `MockMvc` for key REST scenarios.
- A Spring Boot smoke test that starts the application context on a dedicated `test` profile.

Covered scenarios include:

- booking date validation
- resident cancellation rules
- default space activation
- free-space search validation
- successful booking creation
- validation errors for malformed booking payloads
- access control for resident-only actions
- location listing for authenticated users

The `test` profile uses an in-memory H2 database, so the test suite does not depend on a locally running PostgreSQL instance.

## CI

GitHub Actions is configured to validate the repository on pushes to `master` and on pull requests.

The workflow currently runs:

- backend tests with Maven
- frontend type checking
- frontend production build

Main test classes:

- `src/test/java/ru/ifmo/coworkly/booking/BookingServiceTest.java`
- `src/test/java/ru/ifmo/coworkly/space/SpaceServiceTest.java`
- `src/test/java/ru/ifmo/coworkly/booking/BookingControllerWebMvcTest.java`
- `src/test/java/ru/ifmo/coworkly/location/LocationControllerWebMvcTest.java`
- `src/test/java/ru/ifmo/coworkly/CoworklyApplicationTests.java`

## Notes

- For production, move database, JWT, and CORS configuration to environment variables or external config.
- SQL artifacts and database design files may also exist in adjacent repository directories such as `stage2/`.
