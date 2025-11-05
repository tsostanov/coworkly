SET search_path TO s367550, public;

CREATE INDEX IF NOT EXISTS idx_booking_space_time
  ON booking(space_id, starts_at, ends_at);

CREATE INDEX IF NOT EXISTS idx_booking_user
  ON booking(user_id, starts_at DESC);

CREATE INDEX IF NOT EXISTS idx_space_location_active
  ON space(location_id, is_active);

CREATE INDEX IF NOT EXISTS idx_payment_invoice_status
  ON payment(invoice_id, status);

CREATE INDEX IF NOT EXISTS idx_invoice_booking
  ON invoice(booking_id);

CREATE INDEX IF NOT EXISTS idx_access_pass_valid
  ON access_pass(valid_from, valid_to, is_active);

CREATE INDEX IF NOT EXISTS idx_incident_user_created
  ON incident(user_id, created_at DESC);
