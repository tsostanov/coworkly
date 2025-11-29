CREATE TABLE IF NOT EXISTS visit (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    space_id BIGINT NOT NULL,
    check_in TIMESTAMPTZ NOT NULL,
    planned_end TIMESTAMPTZ NOT NULL,
    check_out TIMESTAMPTZ,
    status VARCHAR(50) NOT NULL CHECK (status IN ('ACTIVE','COMPLETED','OVERDUE'))
);

CREATE INDEX IF NOT EXISTS visit_booking_idx ON visit(booking_id);
CREATE INDEX IF NOT EXISTS visit_active_idx ON visit(status) WHERE status = 'ACTIVE';
