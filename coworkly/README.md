# Coworkly

Coworkly — учебный full-stack проект для управления коворкинг-пространством.

## Технологии

### Backend
- Java 17
- Spring Boot 3
- Spring Web / Data JPA / Validation / Security
- Flyway
- PostgreSQL
- JWT (jjwt)
- Maven

### Frontend
- React 18
- TypeScript
- Vite

## Структура проекта

```text
coworkly/
├── src/                     # backend (Spring Boot)
├── frontend/                # frontend (React + Vite)
├── pom.xml                  # Maven-конфигурация backend
└── README.md
```

## Быстрый старт

### 1) Требования
- JDK 17+
- Maven 3.9+ (или запуск через `./mvnw`)
- Node.js 18+
- npm 9+
- PostgreSQL 14+

### 2) Запуск backend

Из корня проекта:

```bash
./mvnw spring-boot:run
```

По умолчанию backend стартует на `http://localhost:8080`.

### 3) Запуск frontend

В отдельном терминале:

```bash
cd frontend
npm install
npm run dev
```

По умолчанию frontend стартует на `http://localhost:5173`.

## Сборка

### Backend
```bash
./mvnw clean package
```

### Frontend
```bash
cd frontend
npm run build
```

## Полезные команды

### Проверка TypeScript
```bash
cd frontend
npm run lint
```

### Генерация PlantUML диаграммы классов (backend)
```bash
./mvnw verify
```

Диаграмма будет сгенерирована в `target/uml/classes.puml`.

## Примечания
- Для production рекомендуется вынести конфигурацию БД, JWT и CORS в переменные окружения.
- SQL-артефакты и схема БД могут находиться в соседних директориях репозитория (например, `stage2/`).
