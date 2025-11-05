-- работаем в своей схеме
SET search_path TO s367550, public;

-- сносим ТОЛЬКО курсовые таблицы
DROP TRIGGER IF EXISTS trg_booking_status ON booking;
DROP TRIGGER IF EXISTS trg_set_total ON booking;
DROP TRIGGER IF EXISTS trg_no_overlap ON booking;

DROP TABLE IF EXISTS incident CASCADE;
DROP TABLE IF EXISTS access_pass CASCADE;
DROP TABLE IF EXISTS payment CASCADE;
DROP TABLE IF EXISTS invoice CASCADE;
DROP TABLE IF EXISTS booking_equipment CASCADE;
DROP TABLE IF EXISTS booking CASCADE;
DROP TABLE IF EXISTS space_equipment CASCADE;
DROP TABLE IF EXISTS equipment CASCADE;
DROP TABLE IF EXISTS space CASCADE;
DROP TABLE IF EXISTS tariff_plan CASCADE;
DROP TABLE IF EXISTS location CASCADE;
DROP TABLE IF EXISTS membership CASCADE;
DROP TABLE IF EXISTS company CASCADE;
DROP TABLE IF EXISTS user_role CASCADE;
DROP TABLE IF EXISTS role CASCADE;
DROP TABLE IF EXISTS audit_log CASCADE;
DROP TABLE IF EXISTS user_account CASCADE;
DROP TABLE IF EXISTS price_rule CASCADE;

-- типы дропаем, если именно в нашей схеме
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM pg_type
    WHERE typname = 'booking_status'
      AND typnamespace = 's367550'::regnamespace
  ) THEN
    DROP TYPE booking_status;
  END IF;

  IF EXISTS (
    SELECT 1 FROM pg_type
    WHERE typname = 'payment_status'
      AND typnamespace = 's367550'::regnamespace
  ) THEN
    DROP TYPE payment_status;
  END IF;
END $$;
