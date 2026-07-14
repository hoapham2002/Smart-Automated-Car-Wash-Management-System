ALTER TABLE customers
    ADD COLUMN IF NOT EXISTS is_admin BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE IF NOT EXISTS tier_configs (
    tier                VARCHAR(20) PRIMARY KEY,
    vnd_per_point       DECIMAL(12,2) NOT NULL,
    point_multiplier    DECIMAL(5,2)  NOT NULL,
    booking_window_days INT           NOT NULL,
    priority_score      INT           NOT NULL,
    point_validity_days INT           NOT NULL,
    min_lifetime_points INT           NOT NULL
);

INSERT INTO tier_configs (
    tier, vnd_per_point, point_multiplier, booking_window_days,
    priority_score, point_validity_days, min_lifetime_points
) VALUES
    ('MEMBER',   10000, 1.00, 7,  10, 365, 0),
    ('SILVER',   10000, 1.05, 10, 20, 365, 1000),
    ('GOLD',     10000, 1.10, 12, 30, 365, 3000),
    ('PLATINUM', 10000, 1.15, 14, 40, 365, 7000)
ON CONFLICT (tier) DO NOTHING;

CREATE INDEX IF NOT EXISTS idx_points_fifo
    ON customer_points(customer_id, type, points, expires_at, created_at);