# Проверка запуска из чистого клона

Проверка выполняется на новой базе, без импорта `stage2/`, готового `target/`, `node_modules/` и локальных конфигурационных файлов.

1. Клонировать репозиторий в новую папку: `git clone https://github.com/tsostanov/coworkly.git coworkly-check`.
2. Создать пустую PostgreSQL по [README](../README.md#quick-start-from-a-clean-clone). Для отдельной установленной PostgreSQL можно создать базу `coworkly_check` и задать `DB_URL=jdbc:postgresql://localhost:5432/coworkly_check?currentSchema=s367550`, а также `DB_USER` и `DB_PASSWORD`.
3. В `coworkly/` выполнить `./mvnw clean package`, затем запустить backend с профилем `demo`.
4. Убедиться, что Flyway применил шесть миграций `V0`–`V5` и backend работает на `8081`.
5. В `coworkly/frontend/` выполнить `npm ci`, `npm run lint`, `npm run build`, затем `npm run dev`.
6. Из корня выполнить `python scripts/demo-smoke.py` и получить `PASS`.
7. Пройти [демо в браузере](demo.md), затем перезапустить backend и убедиться, что Flyway проверяет существующие миграции, а демо-данные не дублируются.

