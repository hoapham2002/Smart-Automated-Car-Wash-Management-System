-- ============================================================
--  AutoWash Pro – V5: Refresh Tokens
--  Required by A03 ("Implement POST /auth/refresh + lưu RefreshToken vào DB")
--  but missing from the originally provided V1-V4 schema.
-- ============================================================

CREATE TABLE refresh_tokens (
    id          UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token       TEXT        NOT NULL,
    expires_at  TIMESTAMPTZ NOT NULL,
    revoked     BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_refresh_token UNIQUE (token)
);

CREATE INDEX idx_refresh_token_user ON refresh_tokens(user_id) WHERE revoked = FALSE;