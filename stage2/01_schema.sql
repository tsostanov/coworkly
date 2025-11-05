-- версия для helios: в своей схеме, без citext, без CREATE SCHEMA
SET search_path TO s367550, public;

-- типы
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_type
    WHERE typname = 'booking_status'
      AND typnamespace = 's367550'::regnamespace
  ) THEN
    CREATE TYPE booking_status AS ENUM
      ('DRAFT','PENDING','CONFIRMED','CANCELED','COMPLETED','NO_SHOW');
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM pg_type
    WHERE typname = 'payment_status'
      AND typnamespace = 's367550'::regnamespace
  ) THEN
    CREATE TYPE payment_status AS ENUM
      ('NEW','AUTHORIZED','CAPTURED','REFUNDED','FAILED');
  END IF;
END $$;

-- пользователи
CREATE TABLE IF NOT EXISTS user_account (
  id            BIGSERIAL PRIMARY KEY,
  email         TEXT NOT NULL UNIQUE,
  password_hash TEXT NOT NULL,
  full_name     TEXT NOT NULL,
  phone         TEXT,
  created_at    TIMESTAMPTZ DEFAULT now(),
  is_active     BOOLEAN DEFAULT TRUE
);

-- роли
CREATE TABLE IF NOT EXISTS role (
  id   SMALLSERIAL PRIMARY KEY,
  code TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS user_role (
  user_id BIGINT   NOT NULL REFERENCES user_account(id) ON DELETE CASCADE,
  role_id SMALLINT NOT NULL REFERENCES role(id)         ON DELETE CASCADE,
  PRIMARY KEY (user_id, role_id)
);

-- компании и членство
CREATE TABLE IF NOT EXISTS company (
  id             BIGSERIAL PRIMARY KEY,
  name           TEXT NOT NULL UNIQUE,
  tax_id         TEXT,
  balance_cents  BIGINT DEFAULT 0,
  created_at     TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS membership (
  id                   BIGSERIAL PRIMARY KEY,
  user_id              BIGINT NOT NULL REFERENCES user_account(id) ON DELETE CASCADE,
  company_id           BIGINT NOT NULL REFERENCES company(id)      ON DELETE CASCADE,
  role_in_company      TEXT,
  monthly_budget_cents BIGINT,
  UNIQUE (user_id, company_id)
);

-- локации
CREATE TABLE IF NOT EXISTS location (
  id      BIGSERIAL PRIMARY KEY,
  name    TEXT NOT NULL UNIQUE,
  address TEXT NOT NULL
);

-- тарифы
CREATE TABLE IF NOT EXISTS tariff_plan (
  id             BIGSERIAL PRIMARY KEY,
  name           TEXT NOT NULL UNIQUE,
  currency       TEXT NOT NULL DEFAULT 'RUB',
  base_price_cph INT  NOT NULL CHECK (base_price_cph > 0),
  cancel_policy  JSONB DEFAULT '{}'::jsonb
);

-- помещения
CREATE TABLE IF NOT EXISTS space (
  id             BIGSERIAL PRIMARY KEY,
  location_id    BIGINT NOT NULL REFERENCES location(id) ON DELETE CASCADE,
  name           TEXT   NOT NULL,
  capacity       INT    NOT NULL CHECK (capacity > 0),
  type           TEXT   NOT NULL CHECK (type IN ('OPEN_DESK','MEETING_ROOM')),
  tariff_plan_id BIGINT REFERENCES tariff_plan(id),
  is_active      BOOLEAN DEFAULT TRUE,
  CONSTRAINT uq_space_per_location UNIQUE (location_id, name)
);

-- оборудование
CREATE TABLE IF NOT EXISTS equipment (
  id   BIGSERIAL PRIMARY KEY,
  name TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS space_equipment (
  space_id     BIGINT NOT NULL REFERENCES space(id)      ON DELETE CASCADE,
  equipment_id BIGINT NOT NULL REFERENCES equipment(id)  ON DELETE CASCADE,
  qty          INT NOT NULL DEFAULT 1 CHECK (qty > 0),
  PRIMARY KEY (space_id, equipment_id)
);

-- прайс-правила
CREATE TABLE IF NOT EXISTS price_rule (
  id             BIGSERIAL PRIMARY KEY,
  tariff_plan_id BIGINT NOT NULL REFERENCES tariff_plan(id) ON DELETE CASCADE,
  rule_type      TEXT   NOT NULL CHECK (rule_type IN ('PEAK','DISCOUNT','PROMO')),
  dow_mask       BIT(7),
  time_from      TIME,
  time_to        TIME,
  percent        INT,
  promo_code     TEXT
);

-- бронь
CREATE TABLE IF NOT EXISTS booking (
  id          BIGSERIAL PRIMARY KEY,
  user_id     BIGINT NOT NULL REFERENCES user_account(id),
  space_id    BIGINT NOT NULL REFERENCES space(id),
  starts_at   TIMESTAMPTZ NOT NULL,
  ends_at     TIMESTAMPTZ NOT NULL,
  status      booking_status NOT NULL DEFAULT 'DRAFT',
  total_cents BIGINT NOT NULL DEFAULT 0,
  created_at  TIMESTAMPTZ DEFAULT now(),
  CHECK (ends_at > starts_at)
);

CREATE TABLE IF NOT EXISTS booking_equipment (
  booking_id   BIGINT NOT NULL REFERENCES booking(id)   ON DELETE CASCADE,
  equipment_id BIGINT NOT NULL REFERENCES equipment(id) ON DELETE RESTRICT,
  qty          INT NOT NULL CHECK (qty > 0),
  PRIMARY KEY (booking_id, equipment_id)
);

-- финансы
CREATE TABLE IF NOT EXISTS invoice (
  id           BIGSERIAL PRIMARY KEY,
  booking_id   BIGINT UNIQUE REFERENCES booking(id) ON DELETE CASCADE,
  issued_at    TIMESTAMPTZ DEFAULT now(),
  amount_cents BIGINT NOT NULL CHECK (amount_cents >= 0)
);

CREATE TABLE IF NOT EXISTS payment (
  id           BIGSERIAL PRIMARY KEY,
  invoice_id   BIGINT REFERENCES invoice(id) ON DELETE CASCADE,
  status       payment_status NOT NULL DEFAULT 'NEW',
  provider     TEXT,
  provider_ref TEXT,
  amount_cents BIGINT NOT NULL CHECK (amount_cents >= 0),
  created_at   TIMESTAMPTZ DEFAULT now()
);

-- пропуска
CREATE TABLE IF NOT EXISTS access_pass (
  booking_id BIGINT PRIMARY KEY REFERENCES booking(id) ON DELETE CASCADE,
  code_hash  TEXT NOT NULL,
  valid_from TIMESTAMPTZ NOT NULL,
  valid_to   TIMESTAMPTZ NOT NULL,
  is_active  BOOLEAN DEFAULT FALSE,
  CHECK (valid_to > valid_from)
);

-- инциденты
CREATE TABLE IF NOT EXISTS incident (
  id            BIGSERIAL PRIMARY KEY,
  booking_id    BIGINT REFERENCES booking(id) ON DELETE SET NULL,
  user_id       BIGINT REFERENCES user_account(id) ON DELETE SET NULL,
  type          TEXT NOT NULL CHECK (type IN ('NO_SHOW','LATE_LEAVE','NOISE','OTHER')),
  penalty_cents BIGINT DEFAULT 0 CHECK (penalty_cents >= 0),
  created_at    TIMESTAMPTZ DEFAULT now(),
  details       TEXT
);

-- аудит
CREATE TABLE IF NOT EXISTS audit_log (
  id         BIGSERIAL PRIMARY KEY,
  actor_id   BIGINT,
  action     TEXT NOT NULL,
  payload    JSONB,
  created_at TIMESTAMPTZ DEFAULT now()
);
