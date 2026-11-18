# Проверка запуска из чистого клона

Проверка выполняется на новой базе, без импорта `stage2/`, готового `target/`, `node_modules/` и локальных конфигурационных файлов.

1. Клонировать репозиторий в новую папку: `git clone https://github.com/tsostanov/coworkly.git coworkly-check`.
2. Создать пустую PostgreSQL по [README](../README.md#quick-start-from-a-clean-clone). Для отдельной установленной PostgreSQL можно создать базу `coworkly_check` и задать `DB_URL=jdbc:postgresql://localhost:5432/coworkly_check?currentSchema=s367550`, а также `DB_USER` и `DB_PASSWORD`.
3. В `coworkly/` выполнить `./mvnw clean package`, затем запустить backend с профилем `demo`.
4. Убедиться, что Flyway применил шесть миграций `V0`–`V5` и backend работает на `8081`.
5. В `coworkly/frontend/` выполнить `npm ci`, `npm run lint`, `npm run build`, затем `npm run dev`.
6. Из корня выполнить `python scripts/demo-smoke.py` и получить `PASS`.
7. Пройти [демо в браузере](demo.md), затем перезапустить backend и убедиться, что Flyway проверяет существующие миграции, а демо-данные не дублируются.

## Результат локального прогона — 18 ноября 2026

Проверен коммит `3020ab1` из отдельного клона `coworkly-clean-check` без готовой сборки и зависимостей фронтенда. Использовались Windows, JDK 17, Node.js и отдельный экземпляр PostgreSQL 18.0 на `127.0.0.1:55432` с новой базой `coworkly_clean_check`. До запуска в схеме приложения было **0 таблиц**.

| Проверка | Результат |
| --- | --- |
| `mvnw.cmd -B clean package` | BUILD SUCCESS; 16 тестов, 0 ошибок |
| `npm ci`, `npm run lint`, `npm run build` | Успешно |
| Запуск backend на пустой PostgreSQL | Применены все 6 миграций V0–V5 |
| `python scripts/demo-smoke.py` | PASS: бронь #1, визит #1; цена, роли, подтверждение, приход, запрет дубликата и уход |
| Сценарий через браузер Edge / Playwright | PASS: резидент #2 создал бронь #2, администратор подтвердил и отметил приход; 0 ошибок JavaScript |
| Перезапуск backend на той же базе | Миграции валидны, схема актуальна; демо-данные не дублируются; повторный smoke PASS |
| `docker compose config --quiet` | Конфигурация корректна |

Docker Engine локально не запускался: для проверки использовался установленный PostgreSQL. Compose и CI настроены на PostgreSQL 16; job `PostgreSQL Demo` проверяет его на GitHub Actions. Flyway из текущего Spring Boot выводит рекомендацию обновления для PostgreSQL 18; все миграции и сценарий на нём прошли.
