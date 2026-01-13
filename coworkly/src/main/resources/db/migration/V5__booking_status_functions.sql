SET search_path TO s367550, public;

CREATE OR REPLACE FUNCTION api_cancel_booking(p_booking_id bigint)
RETURNS void
LANGUAGE plpgsql
AS $$
BEGIN
  UPDATE booking
  SET status = 'CANCELED'
  WHERE id = p_booking_id
    AND status <> 'CANCELED';
END;
$$;

CREATE OR REPLACE FUNCTION api_finalize_bookings()
RETURNS integer
LANGUAGE plpgsql
AS $$
DECLARE
  updated_count integer;
BEGIN
  UPDATE booking
  SET status = CASE
    WHEN status = 'CONFIRMED' THEN 'COMPLETED'
    WHEN status = 'PENDING' THEN 'NO_SHOW'
    ELSE status
  END
  WHERE status IN ('CONFIRMED', 'PENDING')
    AND ends_at <= now();

  GET DIAGNOSTICS updated_count = ROW_COUNT;
  RETURN updated_count;
END;
$$;
