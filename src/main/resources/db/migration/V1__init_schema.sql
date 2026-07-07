-- ============================================================
--  AutoWash Pro – Database Schema
--  PostgreSQL 15+
--  V1: extensions, enums, sequences, tables, FK closures
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- ============================================================
--  ENUMS
-- ============================================================
CREATE TYPE user_role          AS ENUM ('customer','staff','admin');
CREATE TYPE vehicle_size       AS ENUM ('motorbike','scooter','sport_bike','electric_bike');
CREATE TYPE booking_status     AS ENUM ('pending','confirmed','checked_in','in_progress','completed','cancelled','no_show');
CREATE TYPE wash_status        AS ENUM ('queued','washing','drying','done','issue_found');
CREATE TYPE payment_status     AS ENUM ('pending','paid','cancelled');
CREATE TYPE payment_method     AS ENUM ('cash','transfer');          -- no online payment per spec
CREATE TYPE loyalty_tier       AS ENUM ('member','silver','gold','platinum');
CREATE TYPE loyalty_tx_type    AS ENUM ('earn','redeem','expire','adjust','bonus');
CREATE TYPE redemption_type    AS ENUM ('discount','free_wash','addon');
CREATE TYPE promo_target       AS ENUM ('all','member','silver','gold','platinum');
CREATE TYPE promo_type         AS ENUM ('bonus_points','discount_pct','free_wash','double_points');
CREATE TYPE survey_channel     AS ENUM ('facebook','tiktok','group','onsite','app');

-- ============================================================
--  SEQUENCES
-- ============================================================
CREATE SEQUENCE booking_seq START 1 CACHE 10;
CREATE SEQUENCE invoice_seq START 1 CACHE 10;
CREATE SEQUENCE tier_review_seq START 1;

-- ============================================================
--  1. USERS
-- ============================================================
CREATE TABLE users (
    id              UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    phone           VARCHAR(15)  NOT NULL,
    email           VARCHAR(120),
    full_name       VARCHAR(120) NOT NULL,
    password_hash   TEXT         NOT NULL,
    role            user_role    NOT NULL DEFAULT 'customer',
    date_of_birth   DATE,
    gender          VARCHAR(10),
    occupation      VARCHAR(80),
    acquisition_channel VARCHAR(50),
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_users_phone UNIQUE (phone),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT chk_users_phone CHECK (phone ~ '^0[0-9]{9,10}$')
);

CREATE INDEX idx_users_role ON users(role);

-- ============================================================
--  2. VEHICLES
-- ============================================================
CREATE TABLE vehicles (
    id               UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    owner_id         UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    plate_number     VARCHAR(20)  NOT NULL,
    plate_normalized VARCHAR(20)  NOT NULL,
    vehicle_size     vehicle_size NOT NULL DEFAULT 'motorbike',
    brand            VARCHAR(60),
    model            VARCHAR(60),
    color            VARCHAR(40),
    year             SMALLINT,
    is_primary       BOOLEAN      NOT NULL DEFAULT FALSE,
    is_active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_vehicle_plate   UNIQUE (plate_normalized),
    CONSTRAINT chk_vehicle_year   CHECK (year IS NULL OR year BETWEEN 1990 AND 2030)
);

CREATE INDEX idx_vehicles_plate ON vehicles USING gin(plate_normalized gin_trgm_ops);
CREATE INDEX idx_vehicles_owner ON vehicles(owner_id);

-- ============================================================
--  3. LOYALTY TIERS CONFIG
-- ============================================================
CREATE TABLE tier_configs (
    tier                loyalty_tier  PRIMARY KEY,
    min_points          INT           NOT NULL,
    min_visits          INT           NOT NULL DEFAULT 0,
    booking_window_days INT           NOT NULL,
    queue_priority      INT           NOT NULL,
    point_multiplier    NUMERIC(4,2)  NOT NULL DEFAULT 1.00,
    birthday_bonus_pts  INT           NOT NULL DEFAULT 0,
    free_wash_threshold INT,
    description         TEXT,
    updated_at          TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_tier_booking_window CHECK (booking_window_days BETWEEN 1 AND 30),
    CONSTRAINT chk_tier_priority       CHECK (queue_priority BETWEEN 1 AND 10),
    CONSTRAINT chk_tier_multiplier     CHECK (point_multiplier BETWEEN 0.5 AND 10.0)
);

INSERT INTO tier_configs VALUES
    ('member',   0,    0,  7,  1, 1.00, 0,   500, 'Thành viên cơ bản'),
    ('silver',   500,  5,  10, 2, 1.25, 50,  450, 'Thành viên Bạc – 10 ngày đặt trước'),
    ('gold',     2000, 15, 12, 3, 1.50, 100, 400, 'Thành viên Vàng – 12 ngày đặt trước'),
    ('platinum', 5000, 30, 14, 4, 2.00, 200, 350, 'Thành viên Bạch Kim – ưu tiên tuyệt đối');

-- ============================================================
--  4. LOYALTY ACCOUNTS
-- ============================================================
CREATE TABLE loyalty_accounts (
    id                  UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id             UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    current_tier        loyalty_tier NOT NULL DEFAULT 'member',
    points_balance      INT          NOT NULL DEFAULT 0,
    points_ytd          INT          NOT NULL DEFAULT 0,
    total_points_earned INT          NOT NULL DEFAULT 0,
    total_points_spent  INT          NOT NULL DEFAULT 0,
    total_visits        INT          NOT NULL DEFAULT 0,
    total_spend         NUMERIC(14,0) NOT NULL DEFAULT 0,
    tier_achieved_at    TIMESTAMPTZ,
    next_review_at      DATE,
    last_visit_at       TIMESTAMPTZ,
    streak_days         INT          NOT NULL DEFAULT 0,
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_loyalty_user          UNIQUE (user_id),
    CONSTRAINT chk_points_balance       CHECK (points_balance >= 0),
    CONSTRAINT chk_points_ytd           CHECK (points_ytd >= 0),
    CONSTRAINT chk_total_points_earned  CHECK (total_points_earned >= 0),
    CONSTRAINT chk_total_visits         CHECK (total_visits >= 0),
    CONSTRAINT chk_total_spend          CHECK (total_spend >= 0)
);

CREATE INDEX idx_loyalty_tier   ON loyalty_accounts(current_tier);
CREATE INDEX idx_loyalty_review ON loyalty_accounts(next_review_at) WHERE next_review_at IS NOT NULL;

-- ============================================================
--  5. LOYALTY TRANSACTIONS
-- ============================================================
CREATE TABLE loyalty_transactions (
    id              UUID             PRIMARY KEY DEFAULT uuid_generate_v4(),
    account_id      UUID             NOT NULL REFERENCES loyalty_accounts(id),
    booking_id      UUID,
    tx_type         loyalty_tx_type  NOT NULL,
    points          INT              NOT NULL,
    balance_after   INT              NOT NULL,
    multiplier_used NUMERIC(4,2),
    promo_id        UUID,
    note            VARCHAR(255),
    expires_at      DATE,
    created_at      TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_loyalty_tx_balance CHECK (balance_after >= 0)
);

CREATE INDEX idx_loyalty_tx_account ON loyalty_transactions(account_id, created_at DESC);
CREATE INDEX idx_loyalty_tx_expiry  ON loyalty_transactions(expires_at)
    WHERE expires_at IS NOT NULL;

-- ============================================================
--  6. SERVICES & PRICING
-- ============================================================
CREATE TABLE services (
    id           UUID          PRIMARY KEY DEFAULT uuid_generate_v4(),
    name         VARCHAR(120)  NOT NULL,
    description  TEXT,
    duration_min INT           NOT NULL DEFAULT 15,
    base_points  INT           NOT NULL DEFAULT 0,
    is_active    BOOLEAN       NOT NULL DEFAULT TRUE,
    sort_order   INT           NOT NULL DEFAULT 0,
    CONSTRAINT uq_service_name    UNIQUE (name),
    CONSTRAINT chk_duration       CHECK (duration_min BETWEEN 5 AND 120),
    CONSTRAINT chk_base_points    CHECK (base_points >= 0)
);

CREATE TABLE service_prices (
    id           UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    service_id   UUID         NOT NULL REFERENCES services(id) ON DELETE CASCADE,
    vehicle_size vehicle_size NOT NULL,
    price        NUMERIC(12,0) NOT NULL,
    CONSTRAINT uq_service_price  UNIQUE (service_id, vehicle_size),
    CONSTRAINT chk_price_pos     CHECK (price > 0)
);

-- ============================================================
--  7. BOOKING SLOTS
-- ============================================================
CREATE TABLE booking_slots (
    id              UUID    PRIMARY KEY DEFAULT uuid_generate_v4(),
    slot_date       DATE    NOT NULL,
    slot_start      TIME    NOT NULL,
    slot_end        TIME    NOT NULL,
    capacity        INT     NOT NULL DEFAULT 3,
    booked_count    INT     NOT NULL DEFAULT 0,
    is_blocked      BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_slot           UNIQUE (slot_date, slot_start),
    CONSTRAINT chk_slot_times    CHECK (slot_end > slot_start),
    CONSTRAINT chk_slot_count    CHECK (booked_count >= 0),
    CONSTRAINT chk_slot_capacity CHECK (capacity > 0),
    CONSTRAINT chk_no_overbook   CHECK (booked_count <= capacity)
);

CREATE INDEX idx_slots_date  ON booking_slots(slot_date);
CREATE INDEX idx_slots_avail ON booking_slots(slot_date) WHERE is_blocked = FALSE;

-- ============================================================
--  8. BOOKINGS
-- ============================================================
CREATE TABLE bookings (
    id               UUID           PRIMARY KEY DEFAULT uuid_generate_v4(),
    booking_code     VARCHAR(16)    NOT NULL,
    customer_id      UUID           NOT NULL REFERENCES users(id),
    vehicle_id       UUID           NOT NULL REFERENCES vehicles(id),
    service_id       UUID           NOT NULL REFERENCES services(id),
    slot_id          UUID           REFERENCES booking_slots(id),
    status           booking_status NOT NULL DEFAULT 'pending',
    is_walkin        BOOLEAN        NOT NULL DEFAULT FALSE,
    tier_at_booking  loyalty_tier   NOT NULL DEFAULT 'member',
    queue_priority   INT            NOT NULL DEFAULT 1,
    scheduled_at     TIMESTAMPTZ,
    checked_in_at    TIMESTAMPTZ,
    completed_at     TIMESTAMPTZ,
    base_price       NUMERIC(12,0)  NOT NULL,
    discount_amount  NUMERIC(12,0)  NOT NULL DEFAULT 0,
    final_price      NUMERIC(12,0)  NOT NULL,
    points_earned    INT            NOT NULL DEFAULT 0,
    points_redeemed  INT            NOT NULL DEFAULT 0,
    redemption_type  redemption_type,
    used_promo_id    UUID,
    staff_note       TEXT,
    customer_note    TEXT,
    booking_channel  VARCHAR(30)    DEFAULT 'app',
    weather_condition VARCHAR(20),
    created_at       TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_booking_code      UNIQUE (booking_code),
    CONSTRAINT chk_booking_price    CHECK (base_price > 0 AND final_price >= 0),
    CONSTRAINT chk_booking_discount CHECK (discount_amount >= 0 AND discount_amount <= base_price),
    CONSTRAINT chk_booking_schedule CHECK (is_walkin = TRUE OR scheduled_at IS NOT NULL),
    CONSTRAINT chk_booking_times    CHECK (
        completed_at IS NULL OR checked_in_at IS NULL OR
        completed_at >= checked_in_at
    )
);

CREATE INDEX idx_bookings_customer ON bookings(customer_id);
CREATE INDEX idx_bookings_vehicle  ON bookings(vehicle_id);
CREATE INDEX idx_bookings_status   ON bookings(status);
CREATE INDEX idx_bookings_date     ON bookings(scheduled_at DESC);
CREATE INDEX idx_bookings_slot     ON bookings(slot_id) WHERE slot_id IS NOT NULL;
CREATE INDEX idx_bookings_tier     ON bookings(tier_at_booking);

-- ============================================================
--  9. WASH SESSIONS
-- ============================================================
CREATE TABLE wash_sessions (
    id             UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    booking_id     UUID        NOT NULL REFERENCES bookings(id),
    bay_number     INT,
    assigned_staff UUID        REFERENCES users(id),
    status         wash_status NOT NULL DEFAULT 'queued',
    started_at     TIMESTAMPTZ,
    completed_at   TIMESTAMPTZ,
    quality_score  NUMERIC(3,1),
    customer_rating SMALLINT,
    customer_feedback TEXT,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_wash_booking    UNIQUE (booking_id),
    CONSTRAINT chk_quality_score  CHECK (quality_score IS NULL OR quality_score BETWEEN 0 AND 10),
    CONSTRAINT chk_customer_rating CHECK (customer_rating IS NULL OR customer_rating BETWEEN 1 AND 5)
);

CREATE INDEX idx_wash_status ON wash_sessions(status);

-- ============================================================
-- 10. INVOICES & PAYMENTS
-- ============================================================
CREATE TABLE invoices (
    id              UUID           PRIMARY KEY DEFAULT uuid_generate_v4(),
    invoice_code    VARCHAR(20)    NOT NULL,
    booking_id      UUID           NOT NULL REFERENCES bookings(id),
    customer_id     UUID           NOT NULL REFERENCES users(id),
    subtotal        NUMERIC(12,0)  NOT NULL,
    discount_amount NUMERIC(12,0)  NOT NULL DEFAULT 0,
    total_amount    NUMERIC(12,0)  NOT NULL,
    status          payment_status NOT NULL DEFAULT 'pending',
    paid_at         TIMESTAMPTZ,
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_invoice_code     UNIQUE (invoice_code),
    CONSTRAINT uq_invoice_booking  UNIQUE (booking_id),
    CONSTRAINT chk_invoice_amounts CHECK (subtotal > 0 AND discount_amount >= 0 AND total_amount >= 0)
);

CREATE TABLE payments (
    id           UUID           PRIMARY KEY DEFAULT uuid_generate_v4(),
    invoice_id   UUID           NOT NULL REFERENCES invoices(id),
    amount       NUMERIC(12,0)  NOT NULL,
    method       payment_method NOT NULL,
    status       payment_status NOT NULL DEFAULT 'pending',
    note         TEXT,
    processed_at TIMESTAMPTZ,
    created_at   TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_payment_amount CHECK (amount > 0)
);

CREATE INDEX idx_invoices_customer ON invoices(customer_id);
CREATE INDEX idx_payments_invoice  ON payments(invoice_id);

-- ============================================================
-- 11. PROMOTIONS
-- ============================================================
CREATE TABLE promotions (
    id              UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    code            VARCHAR(30)  UNIQUE,
    name            VARCHAR(120) NOT NULL,
    description     TEXT,
    promo_type      promo_type   NOT NULL,
    target_tier     promo_target NOT NULL DEFAULT 'all',
    discount_pct    NUMERIC(5,2),
    bonus_points    INT,
    multiplier      NUMERIC(4,2),
    min_spend       NUMERIC(12,0) NOT NULL DEFAULT 0,
    min_visits      INT           NOT NULL DEFAULT 0,
    usage_limit_total INT,
    usage_limit_per_user INT DEFAULT 1,
    used_count      INT           NOT NULL DEFAULT 0,
    valid_from      TIMESTAMPTZ   NOT NULL,
    valid_to        TIMESTAMPTZ   NOT NULL,
    is_active       BOOLEAN       NOT NULL DEFAULT TRUE,
    created_by      UUID          REFERENCES users(id),
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_promo_dates    CHECK (valid_to > valid_from),
    CONSTRAINT chk_promo_discount CHECK (discount_pct IS NULL OR discount_pct BETWEEN 0.01 AND 100),
    CONSTRAINT chk_promo_bonus    CHECK (bonus_points IS NULL OR bonus_points > 0),
    CONSTRAINT chk_promo_mult     CHECK (multiplier IS NULL OR multiplier BETWEEN 1.0 AND 10.0)
);

CREATE INDEX idx_promos_active ON promotions(valid_from, valid_to, target_tier) WHERE is_active = TRUE;

CREATE TABLE promotion_usages (
    id           UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    promo_id     UUID        NOT NULL REFERENCES promotions(id),
    user_id      UUID        NOT NULL REFERENCES users(id),
    booking_id   UUID        NOT NULL REFERENCES bookings(id),
    benefit_value NUMERIC(12,0),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_promo_user_booking UNIQUE (promo_id, booking_id)
);

CREATE INDEX idx_promo_usage_user  ON promotion_usages(user_id);
CREATE INDEX idx_promo_usage_promo ON promotion_usages(promo_id);

-- ============================================================
-- 12. REDEMPTION CATALOG
-- ============================================================
CREATE TABLE redemption_options (
    id              UUID            PRIMARY KEY DEFAULT uuid_generate_v4(),
    name            VARCHAR(120)    NOT NULL,
    description     TEXT,
    redemption_type redemption_type NOT NULL,
    points_required INT             NOT NULL,
    discount_value  NUMERIC(12,0),
    service_id      UUID            REFERENCES services(id),
    min_tier        loyalty_tier    NOT NULL DEFAULT 'member',
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_redemption_points CHECK (points_required > 0)
);

-- ============================================================
-- 13. POINT EXPIRY BATCHES
-- ============================================================
CREATE TABLE point_expiry_batches (
    id              UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    account_id      UUID        NOT NULL REFERENCES loyalty_accounts(id),
    points_original INT         NOT NULL,
    points_remaining INT        NOT NULL,
    earned_at       TIMESTAMPTZ NOT NULL,
    expires_at      DATE        NOT NULL,
    is_expired      BOOLEAN     NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_expiry_points CHECK (points_remaining >= 0 AND points_remaining <= points_original)
);

CREATE INDEX idx_expiry_account ON point_expiry_batches(account_id);
CREATE INDEX idx_expiry_date    ON point_expiry_batches(expires_at) WHERE is_expired = FALSE;

-- ============================================================
-- 14. SURVEY & RESEARCH DATA
-- ============================================================
CREATE TABLE survey_responses (
    id                  UUID           PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id             UUID           REFERENCES users(id),
    channel             survey_channel NOT NULL,
    q_loyalty_program_importance  SMALLINT,
    q_tier_upgrade_motivation     SMALLINT,
    q_booking_convenience         SMALLINT,
    q_price_sensitivity           SMALLINT,
    q_service_quality_importance  SMALLINT,
    q_frequency_per_month         SMALLINT,
    q_preferred_wash_time         VARCHAR(20),
    q_referral_likelihood         SMALLINT,
    free_text                     TEXT,
    is_synthetic                  BOOLEAN     NOT NULL DEFAULT FALSE,
    is_valid                      BOOLEAN     NOT NULL DEFAULT TRUE,
    submitted_at                  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_survey_likert  CHECK (
        (q_loyalty_program_importance BETWEEN 1 AND 5 OR q_loyalty_program_importance IS NULL) AND
        (q_tier_upgrade_motivation    BETWEEN 1 AND 5 OR q_tier_upgrade_motivation    IS NULL) AND
        (q_booking_convenience        BETWEEN 1 AND 5 OR q_booking_convenience        IS NULL) AND
        (q_price_sensitivity          BETWEEN 1 AND 5 OR q_price_sensitivity          IS NULL) AND
        (q_service_quality_importance BETWEEN 1 AND 5 OR q_service_quality_importance IS NULL)
    )
);

CREATE INDEX idx_survey_user    ON survey_responses(user_id) WHERE user_id IS NOT NULL;
CREATE INDEX idx_survey_channel ON survey_responses(channel);
CREATE INDEX idx_survey_synthetic ON survey_responses(is_synthetic);

-- ============================================================
-- 15. BEHAVIORAL LOG
-- ============================================================
CREATE TABLE behavioral_events (
    id          BIGSERIAL   PRIMARY KEY,
    user_id     UUID        REFERENCES users(id),
    event_type  VARCHAR(60) NOT NULL,
    event_data  JSONB       NOT NULL,
    tier_at_event loyalty_tier,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_behavioral_user      ON behavioral_events(user_id, created_at DESC);
CREATE INDEX idx_behavioral_type      ON behavioral_events(event_type);
CREATE INDEX idx_behavioral_time      ON behavioral_events(created_at DESC);
CREATE INDEX idx_behavioral_tier      ON behavioral_events(tier_at_event);
CREATE INDEX idx_behavioral_data_gin  ON behavioral_events USING gin(event_data);

-- ============================================================
-- 16. TIER UPGRADE HISTORY
-- ============================================================
CREATE TABLE tier_history (
    id           UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    account_id   UUID         NOT NULL REFERENCES loyalty_accounts(id),
    from_tier    loyalty_tier,
    to_tier      loyalty_tier NOT NULL,
    reason       VARCHAR(60)  NOT NULL,
    points_at_change  INT     NOT NULL,
    visits_at_change  INT     NOT NULL,
    spend_at_change   NUMERIC(14,0) NOT NULL,
    changed_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_tier_history_account ON tier_history(account_id, changed_at DESC);
CREATE INDEX idx_tier_history_reason  ON tier_history(reason);

-- ============================================================
-- 17. FOREIGN KEY CLOSURES (added after all tables exist, avoids circular deps)
-- ============================================================
ALTER TABLE loyalty_transactions
    ADD CONSTRAINT fk_loyalty_tx_booking
    FOREIGN KEY (booking_id) REFERENCES bookings(id);

ALTER TABLE loyalty_transactions
    ADD CONSTRAINT fk_loyalty_tx_promo
    FOREIGN KEY (promo_id) REFERENCES promotions(id);

ALTER TABLE bookings
    ADD CONSTRAINT fk_booking_promo
    FOREIGN KEY (used_promo_id) REFERENCES promotions(id);
