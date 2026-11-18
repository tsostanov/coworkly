# Coworkly

[![CI](https://github.com/tsostanov/coworkly/actions/workflows/ci.yml/badge.svg?branch=master&event=push)](https://github.com/tsostanov/coworkly/actions/workflows/ci.yml)

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

Use the [repository quick start](../README.md#quick-start-from-a-clean-clone) to create an empty PostgreSQL database, apply Flyway migrations and start both applications. The Maven module is this directory; the frontend is `frontend/`.

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=demo
```

Windows PowerShell: `./mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=demo"`.

The optional `demo` profile creates local accounts and bookable workspaces. Follow the [resident booking / admin visit scenario](../docs/demo.md).

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

- Supply `DB_URL`, `DB_USER`, `DB_PASSWORD` and `JWT_SECRET` for your environment; local defaults are documented in the repository README.
- SQL artifacts and database design files may also exist in adjacent repository directories such as `stage2/`.
