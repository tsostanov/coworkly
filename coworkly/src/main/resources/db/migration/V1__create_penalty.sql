CREATE TABLE IF NOT EXISTS penalty (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    reason TEXT,
    limit_minutes INT,
    amount_cents BIGINT,
    expires_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    revoked_at TIMESTAMPTZ,
    created_by_admin_id BIGINT,
    CONSTRAINT penalty_type_chk CHECK (type IN ('TIMEOUT','MAX_DURATION_LIMIT','FINE'))
);

CREATE INDEX IF NOT EXISTS penalty_user_active_idx
    ON penalty (user_id)
    WHERE revoked_at IS NULL;
