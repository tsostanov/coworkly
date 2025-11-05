SET search_path TO s367550, public;

-- утилита для маски дней
CREATE OR REPLACE FUNCTION in_dow_mask(p_mask BIT(7), p_ts TIMESTAMPTZ)
RETURNS BOOLEAN
LANGUAGE sql
IMMUTABLE
AS $$
  SELECT CASE WHEN p_mask IS NULL THEN TRUE
              ELSE get_bit(p_mask, EXTRACT(DOW FROM p_ts)::INT) = 1 END;
$$;

-- расчёт цены
CREATE OR REPLACE FUNCTION calc_booking_price(
  p_space BIGINT,
  p_from  TIMESTAMPTZ,
  p_to    TIMESTAMPTZ,
  p_promo TEXT
)
RETURNS BIGINT
LANGUAGE plpgsql
AS $$
DECLARE
  tp_id     BIGINT;
  base_cph  INT;
  dur_hours NUMERIC;
  amount    NUMERIC;
  r         RECORD;
  hh        TIME;
BEGIN
  SELECT s.tariff_plan_id, t.base_price_cph
    INTO tp_id, base_cph
  FROM space s
  JOIN tariff_plan t ON t.id = s.tariff_plan_id
  WHERE s.id = p_space;

  IF tp_id IS NULL THEN
    RAISE EXCEPTION 'Tariff plan is not set for space %', p_space;
  END IF;

  dur_hours := EXTRACT(EPOCH FROM (p_to - p_from)) / 3600.0;
  amount := CEIL(base_cph * dur_hours);

  FOR r IN SELECT * FROM price_rule WHERE tariff_plan_id = tp_id LOOP
    hh := (p_from AT TIME ZONE 'UTC')::TIME;
    IF in_dow_mask(r.dow_mask, p_from)
       AND (r.promo_code IS NULL OR r.promo_code = p_promo)
       AND (r.time_from IS NULL OR hh >= r.time_from)
       AND (r.time_to   IS NULL OR hh <  r.time_to) THEN
      amount := CEIL(amount * (100 + COALESCE(r.percent,0)) / 100.0);
    END IF;
  END LOOP;

  RETURN amount::BIGINT;
END $$;

-- запрет пересечений
CREATE OR REPLACE FUNCTION ensure_no_overlap()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM booking b
    WHERE b.space_id = NEW.space_id
      AND b.status IN ('PENDING','CONFIRMED')
      AND tstzrange(b.starts_at, b.ends_at, '[)')
          && tstzrange(NEW.starts_at, NEW.ends_at, '[)')
      AND b.id <> COALESCE(NEW.id, -1)
  ) THEN
    RAISE EXCEPTION 'Overlap booking detected for space %', NEW.space_id;
  END IF;
  RETURN NEW;
END $$;

DROP TRIGGER IF EXISTS trg_no_overlap ON booking;
CREATE TRIGGER trg_no_overlap
BEFORE INSERT OR UPDATE ON booking
FOR EACH ROW
EXECUTE FUNCTION ensure_no_overlap();

-- автоподсчёт суммы
CREATE OR REPLACE FUNCTION set_booking_total()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
  NEW.total_cents := calc_booking_price(NEW.space_id, NEW.starts_at, NEW.ends_at, NULL);
  RETURN NEW;
END $$;

DROP TRIGGER IF EXISTS trg_set_total ON booking;
CREATE TRIGGER trg_set_total
BEFORE INSERT OR UPDATE OF space_id, starts_at, ends_at ON booking
FOR EACH ROW
EXECUTE FUNCTION set_booking_total();

-- реакция на смену статуса
CREATE OR REPLACE FUNCTION booking_status_flow()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
  IF NEW.status = 'CONFIRMED'
     AND (OLD.status IS DISTINCT FROM 'CONFIRMED') THEN

    INSERT INTO invoice(booking_id, amount_cents)
    VALUES (NEW.id, NEW.total_cents)
    ON CONFLICT (booking_id) DO NOTHING;

    INSERT INTO access_pass(booking_id, code_hash, valid_from, valid_to, is_active)
    VALUES (
      NEW.id,
      md5(random()::text),
      NEW.starts_at - INTERVAL '10 minutes',
      NEW.ends_at   + INTERVAL '5 minutes',
      FALSE
    )
    ON CONFLICT (booking_id) DO NOTHING;
  END IF;

  IF NEW.status IN ('CANCELED','COMPLETED','NO_SHOW')
     AND OLD.status NOT IN ('CANCELED','COMPLETED','NO_SHOW') THEN
    UPDATE access_pass SET is_active = FALSE WHERE booking_id = NEW.id;
  END IF;

  RETURN NEW;
END $$;

DROP TRIGGER IF EXISTS trg_booking_status ON booking;
CREATE TRIGGER trg_booking_status
AFTER UPDATE OF status ON booking
FOR EACH ROW
EXECUTE FUNCTION booking_status_flow();

-- активировать пропуска
CREATE OR REPLACE FUNCTION activate_due_passes(now_ts TIMESTAMPTZ DEFAULT now())
RETURNS INT
LANGUAGE plpgsql
AS $$
DECLARE
  n INT;
BEGIN
  UPDATE access_pass
     SET is_active = TRUE
   WHERE valid_from <= now_ts
     AND valid_to   >  now_ts
     AND is_active  =  FALSE;
  GET DIAGNOSTICS n = ROW_COUNT;
  RETURN n;
END $$;

-- API
CREATE OR REPLACE FUNCTION api_create_booking(
  p_user  BIGINT,
  p_space BIGINT,
  p_from  TIMESTAMPTZ,
  p_to    TIMESTAMPTZ
)
RETURNS BIGINT
LANGUAGE plpgsql
AS $$
DECLARE
  bid BIGINT;
BEGIN
  INSERT INTO booking(user_id, space_id, starts_at, ends_at, status)
  VALUES (p_user, p_space, p_from, p_to, 'PENDING')
  RETURNING id INTO bid;
  RETURN bid;
END $$;

CREATE OR REPLACE FUNCTION api_confirm_booking(p_bid BIGINT)
RETURNS VOID
LANGUAGE plpgsql
AS $$
BEGIN
  UPDATE booking SET status = 'CONFIRMED' WHERE id = p_bid;
END $$;

CREATE OR REPLACE FUNCTION api_capture_payment(
  p_bid      BIGINT,
  p_amount   BIGINT,
  p_provider TEXT,
  p_ref      TEXT
)
RETURNS VOID
LANGUAGE plpgsql
AS $$
DECLARE
  iid BIGINT;
BEGIN
  SELECT id INTO iid FROM invoice WHERE booking_id = p_bid;
  IF iid IS NULL THEN
    RAISE EXCEPTION 'Invoice missing for booking %', p_bid;
  END IF;

  INSERT INTO payment(invoice_id, status, provider, provider_ref, amount_cents)
  VALUES (iid, 'CAPTURED', p_provider, p_ref, p_amount);

  UPDATE booking SET status = 'COMPLETED' WHERE id = p_bid;
END $$;

CREATE OR REPLACE FUNCTION search_free_spaces(
  p_location BIGINT,
  p_from     TIMESTAMPTZ,
  p_to       TIMESTAMPTZ,
  p_capacity INT DEFAULT 1
)
RETURNS TABLE(space_id BIGINT, space_name TEXT, capacity INT)
LANGUAGE sql
AS $$
  SELECT s.id, s.name, s.capacity
  FROM space s
  WHERE s.is_active
    AND s.location_id = p_location
    AND s.capacity    >= p_capacity
    AND NOT EXISTS (
      SELECT 1 FROM booking b
      WHERE b.space_id = s.id
        AND b.status IN ('PENDING','CONFIRMED')
        AND tstzrange(b.starts_at, b.ends_at, '[)')
            && tstzrange(p_from, p_to, '[)')
    )
  ORDER BY s.capacity, s.name;
$$;
