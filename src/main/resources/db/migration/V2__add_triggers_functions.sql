-- ============================================================
--  AutoWash Pro – V2: Triggers & Functions
-- ============================================================

-- T1. Auto updated_at
CREATE OR REPLACE FUNCTION fn_set_updated_at()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN NEW.updated_at = NOW(); RETURN NEW; END;
$$;

CREATE TRIGGER trg_updated_users
    BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
CREATE TRIGGER trg_updated_bookings
    BEFORE UPDATE ON bookings FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
CREATE TRIGGER trg_updated_loyalty
    BEFORE UPDATE ON loyalty_accounts FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

-- T2. Auto booking_code
CREATE OR REPLACE FUNCTION fn_gen_booking_code()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    IF NEW.booking_code IS NULL OR NEW.booking_code = '' THEN
        NEW.booking_code := 'BK' || TO_CHAR(NOW(),'YYYYMMDD') || '-'
                         || LPAD(nextval('booking_seq')::TEXT, 4, '0');
    END IF;
    RETURN NEW;
END;
$$;
CREATE TRIGGER trg_booking_code
    BEFORE INSERT ON bookings FOR EACH ROW EXECUTE FUNCTION fn_gen_booking_code();

-- T3. Auto invoice_code
CREATE OR REPLACE FUNCTION fn_gen_invoice_code()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    IF NEW.invoice_code IS NULL OR NEW.invoice_code = '' THEN
        NEW.invoice_code := 'INV' || TO_CHAR(NOW(),'YYYYMM') || '-'
                         || LPAD(nextval('invoice_seq')::TEXT, 5, '0');
    END IF;
    RETURN NEW;
END;
$$;
CREATE TRIGGER trg_invoice_code
    BEFORE INSERT ON invoices FOR EACH ROW EXECUTE FUNCTION fn_gen_invoice_code();

-- T4. Slot booked_count maintenance
CREATE OR REPLACE FUNCTION fn_update_slot_count()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        IF NEW.slot_id IS NOT NULL THEN
            UPDATE booking_slots SET booked_count = booked_count + 1 WHERE id = NEW.slot_id;
        END IF;
    ELSIF TG_OP = 'UPDATE' THEN
        IF OLD.slot_id IS DISTINCT FROM NEW.slot_id THEN
            IF OLD.slot_id IS NOT NULL THEN
                UPDATE booking_slots SET booked_count = GREATEST(booked_count-1,0) WHERE id = OLD.slot_id;
            END IF;
            IF NEW.slot_id IS NOT NULL THEN
                UPDATE booking_slots SET booked_count = booked_count + 1 WHERE id = NEW.slot_id;
            END IF;
        END IF;
        IF OLD.status NOT IN ('CANCELLED','NO_SHOW') AND NEW.status IN ('CANCELLED','NO_SHOW')
           AND NEW.slot_id IS NOT NULL THEN
            UPDATE booking_slots SET booked_count = GREATEST(booked_count-1,0) WHERE id = NEW.slot_id;
        END IF;
    ELSIF TG_OP = 'DELETE' THEN
        IF OLD.slot_id IS NOT NULL THEN
            UPDATE booking_slots SET booked_count = GREATEST(booked_count-1,0) WHERE id = OLD.slot_id;
        END IF;
    END IF;
    RETURN COALESCE(NEW, OLD);
END;
$$;
CREATE TRIGGER trg_slot_count
    AFTER INSERT OR UPDATE OR DELETE ON bookings
    FOR EACH ROW EXECUTE FUNCTION fn_update_slot_count();

-- T5. Chặn overbooking + kiểm tra booking_window theo tier
CREATE OR REPLACE FUNCTION fn_check_booking_rules()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE
    v_remaining      INT;
    v_window         INT;
    v_days_ahead     INT;
    v_tier           loyalty_tier;
BEGIN
    SELECT current_tier INTO v_tier
    FROM loyalty_accounts WHERE user_id = NEW.customer_id;
    v_tier := COALESCE(v_tier, 'MEMBER');
    NEW.tier_at_booking := v_tier;

    IF NEW.scheduled_at IS NOT NULL AND NOT NEW.is_walkin THEN
        SELECT booking_window_days INTO v_window
        FROM tier_configs WHERE tier = v_tier;

        v_days_ahead := EXTRACT(DAY FROM NEW.scheduled_at - NOW())::INT;
        IF v_days_ahead > v_window THEN
            RAISE EXCEPTION
                'Tier % chỉ được đặt trước tối đa % ngày (bạn đang đặt % ngày)',
                v_tier, v_window, v_days_ahead
                USING ERRCODE = 'P0010';
        END IF;
    END IF;

    IF NEW.slot_id IS NOT NULL THEN
        SELECT capacity - booked_count INTO v_remaining
        FROM booking_slots WHERE id = NEW.slot_id AND is_blocked = FALSE;

        IF NOT FOUND THEN
            RAISE EXCEPTION 'Slot không tồn tại hoặc đang bị khoá' USING ERRCODE = 'P0001';
        END IF;
        IF v_remaining <= 0 THEN
            RAISE EXCEPTION 'Slot đã hết chỗ' USING ERRCODE = 'P0002';
        END IF;
    END IF;

    SELECT queue_priority INTO NEW.queue_priority
    FROM tier_configs WHERE tier = v_tier;

    RETURN NEW;
END;
$$;
CREATE TRIGGER trg_check_booking_rules
    BEFORE INSERT ON bookings FOR EACH ROW EXECUTE FUNCTION fn_check_booking_rules();

-- T6. Auto init loyalty account khi đăng ký customer
CREATE OR REPLACE FUNCTION fn_init_loyalty()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    IF NEW.role = 'CUSTOMER' THEN
        INSERT INTO loyalty_accounts (user_id, next_review_at)
        VALUES (NEW.id, DATE_TRUNC('month', NOW() + INTERVAL '1 month')::DATE)
        ON CONFLICT (user_id) DO NOTHING;
    END IF;
    RETURN NEW;
END;
$$;
CREATE TRIGGER trg_init_loyalty
    AFTER INSERT ON users FOR EACH ROW EXECUTE FUNCTION fn_init_loyalty();

-- T7. Log behavioral events tự động khi booking thay đổi status
CREATE OR REPLACE FUNCTION fn_log_booking_event()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    IF OLD.status IS DISTINCT FROM NEW.status THEN
        INSERT INTO behavioral_events (user_id, event_type, event_data, tier_at_event)
        VALUES (
            NEW.customer_id,
            'booking_status_' || NEW.status,
            jsonb_build_object(
                'booking_id',     NEW.id,
                'booking_code',   NEW.booking_code,
                'service_id',     NEW.service_id,
                'final_price',    NEW.final_price,
                'points_earned',  NEW.points_earned,
                'from_status',    OLD.status,
                'to_status',      NEW.status,
                'is_walkin',      NEW.is_walkin
            ),
            NEW.tier_at_booking
        );
    END IF;
    RETURN NEW;
END;
$$;
CREATE TRIGGER trg_log_booking_event
    AFTER UPDATE ON bookings FOR EACH ROW EXECUTE FUNCTION fn_log_booking_event();

-- ============================================================
-- FUNCTIONS
-- ============================================================

-- F1. Tích điểm sau khi booking hoàn thành
CREATE OR REPLACE FUNCTION fn_earn_points(p_booking_id UUID)
RETURNS INT LANGUAGE plpgsql AS $$
DECLARE
    v_bk       bookings%ROWTYPE;
    v_svc      services%ROWTYPE;
    v_acc      loyalty_accounts%ROWTYPE;
    v_cfg      tier_configs%ROWTYPE;
    v_promo    promotions%ROWTYPE;
    v_pts_base INT;
    v_pts_final INT;
    v_multiplier NUMERIC(4,2);
    v_expire_date DATE;
BEGIN
    SELECT * INTO v_bk  FROM bookings         WHERE id = p_booking_id;
    SELECT * INTO v_svc FROM services          WHERE id = v_bk.service_id;
    SELECT * INTO v_acc FROM loyalty_accounts  WHERE user_id = v_bk.customer_id FOR UPDATE;
    SELECT * INTO v_cfg FROM tier_configs      WHERE tier    = v_acc.current_tier;

    IF NOT FOUND THEN RETURN 0; END IF;
    IF v_bk.status <> 'COMPLETED' THEN RETURN 0; END IF;

    v_pts_base := FLOOR(v_bk.final_price / 1000)::INT + v_svc.base_points;
    v_multiplier := v_cfg.point_multiplier;

    IF v_bk.used_promo_id IS NOT NULL THEN
        SELECT * INTO v_promo FROM promotions WHERE id = v_bk.used_promo_id;
        IF v_promo.promo_type = 'DOUBLE_POINTS' THEN
            v_multiplier := v_multiplier * COALESCE(v_promo.multiplier, 2.0);
        END IF;
    END IF;

    v_pts_final := CEIL(v_pts_base * v_multiplier)::INT;
    v_expire_date := (NOW() + INTERVAL '12 months')::DATE;

    UPDATE loyalty_accounts SET
        points_balance     = points_balance + v_pts_final,
        points_ytd         = points_ytd + v_pts_final,
        total_points_earned= total_points_earned + v_pts_final,
        total_visits       = total_visits + 1,
        total_spend        = total_spend + v_bk.final_price,
        last_visit_at      = NOW(),
        updated_at         = NOW()
    WHERE user_id = v_bk.customer_id;

    INSERT INTO loyalty_transactions
        (account_id, booking_id, tx_type, points, balance_after,
         multiplier_used, promo_id, note, expires_at)
    VALUES
        (v_acc.id, p_booking_id, 'EARN', v_pts_final,
         v_acc.points_balance + v_pts_final,
         v_multiplier, v_bk.used_promo_id,
         'Tích điểm từ ' || v_bk.booking_code,
         v_expire_date);

    INSERT INTO point_expiry_batches
        (account_id, points_original, points_remaining, earned_at, expires_at)
    VALUES
        (v_acc.id, v_pts_final, v_pts_final, NOW(), v_expire_date);

    UPDATE bookings SET points_earned = v_pts_final WHERE id = p_booking_id;

    INSERT INTO behavioral_events (user_id, event_type, event_data, tier_at_event)
    VALUES (
        v_bk.customer_id,
        'points_earned',
        jsonb_build_object(
            'booking_id',    p_booking_id,
            'points',        v_pts_final,
            'multiplier',    v_multiplier,
            'final_price',   v_bk.final_price
        ),
        v_acc.current_tier
    );

    RETURN v_pts_final;
END;
$$;

-- F2. Đổi điểm lấy reward
CREATE OR REPLACE FUNCTION fn_redeem_points(
    p_user_id    UUID,
    p_option_id  UUID,
    p_booking_id UUID DEFAULT NULL
)
RETURNS JSONB LANGUAGE plpgsql AS $$
DECLARE
    v_acc   loyalty_accounts%ROWTYPE;
    v_opt   redemption_options%ROWTYPE;
BEGIN
    SELECT * INTO v_acc FROM loyalty_accounts WHERE user_id = p_user_id FOR UPDATE;
    SELECT * INTO v_opt FROM redemption_options WHERE id = p_option_id AND is_active = TRUE;

    IF NOT FOUND THEN
        RETURN jsonb_build_object('success', FALSE, 'error', 'OPTION_NOT_FOUND');
    END IF;
    IF v_acc.current_tier < v_opt.min_tier THEN
        RETURN jsonb_build_object('success', FALSE, 'error', 'TIER_TOO_LOW');
    END IF;
    IF v_acc.points_balance < v_opt.points_required THEN
        RETURN jsonb_build_object('success', FALSE, 'error', 'INSUFFICIENT_POINTS',
            'have', v_acc.points_balance, 'need', v_opt.points_required);
    END IF;

    UPDATE loyalty_accounts SET
        points_balance    = points_balance - v_opt.points_required,
        total_points_spent= total_points_spent + v_opt.points_required,
        updated_at        = NOW()
    WHERE user_id = p_user_id;

    INSERT INTO loyalty_transactions
        (account_id, booking_id, tx_type, points, balance_after, note)
    VALUES
        (v_acc.id, p_booking_id, 'REDEEM',
         -v_opt.points_required,
         v_acc.points_balance - v_opt.points_required,
         'Đổi điểm: ' || v_opt.name);

    RETURN jsonb_build_object(
        'success',          TRUE,
        'option_name',      v_opt.name,
        'points_used',      v_opt.points_required,
        'redemption_type',  v_opt.redemption_type,
        'discount_value',   v_opt.discount_value,
        'service_id',       v_opt.service_id
    );
END;
$$;

-- F3. Monthly tier review
CREATE OR REPLACE FUNCTION fn_review_tiers()
RETURNS INT LANGUAGE plpgsql AS $$
DECLARE
    v_account  loyalty_accounts%ROWTYPE;
    v_new_tier loyalty_tier;
    v_count    INT := 0;
BEGIN
    FOR v_account IN
        SELECT * FROM loyalty_accounts
        WHERE next_review_at <= CURRENT_DATE
        FOR UPDATE SKIP LOCKED
    LOOP
        SELECT tier INTO v_new_tier
        FROM tier_configs
        WHERE min_points  <= v_account.points_ytd
          AND min_visits  <= v_account.total_visits
        ORDER BY min_points DESC
        LIMIT 1;

        v_new_tier := COALESCE(v_new_tier, 'MEMBER');

        IF v_new_tier <> v_account.current_tier THEN
            INSERT INTO tier_history
                (account_id, from_tier, to_tier, reason,
                 points_at_change, visits_at_change, spend_at_change)
            VALUES
                (v_account.id, v_account.current_tier, v_new_tier,
                 CASE WHEN v_new_tier > v_account.current_tier THEN 'upgrade' ELSE 'downgrade' END,
                 v_account.points_ytd, v_account.total_visits, v_account.total_spend);

            INSERT INTO behavioral_events (user_id, event_type, event_data, tier_at_event)
            SELECT user_id, 'tier_changed',
                   jsonb_build_object('from', v_account.current_tier, 'to', v_new_tier,
                                      'points_ytd', v_account.points_ytd,
                                      'total_visits', v_account.total_visits),
                   v_new_tier
            FROM loyalty_accounts WHERE id = v_account.id;
        END IF;

        UPDATE loyalty_accounts SET
            current_tier   = v_new_tier,
            points_ytd     = 0,
            next_review_at = (DATE_TRUNC('month', NOW() + INTERVAL '1 month'))::DATE,
            updated_at     = NOW()
        WHERE id = v_account.id;

        v_count := v_count + 1;
    END LOOP;

    RETURN v_count;
END;
$$;

-- F4. Expire điểm quá 12 tháng
CREATE OR REPLACE FUNCTION fn_expire_points()
RETURNS INT LANGUAGE plpgsql AS $$
DECLARE
    v_batch  point_expiry_batches%ROWTYPE;
    v_acc    loyalty_accounts%ROWTYPE;
    v_count  INT := 0;
BEGIN
    FOR v_batch IN
        SELECT * FROM point_expiry_batches
        WHERE expires_at <= CURRENT_DATE
          AND is_expired = FALSE
          AND points_remaining > 0
        FOR UPDATE SKIP LOCKED
    LOOP
        SELECT * INTO v_acc FROM loyalty_accounts WHERE id = v_batch.account_id;

        UPDATE loyalty_accounts SET
            points_balance = GREATEST(points_balance - v_batch.points_remaining, 0),
            updated_at     = NOW()
        WHERE id = v_batch.account_id;

        INSERT INTO loyalty_transactions
            (account_id, tx_type, points, balance_after, note)
        VALUES
            (v_batch.account_id, 'EXPIRE', -v_batch.points_remaining,
             GREATEST(v_acc.points_balance - v_batch.points_remaining, 0),
             'Điểm hết hạn (batch ' || v_batch.id || ')');

        UPDATE point_expiry_batches
        SET is_expired = TRUE WHERE id = v_batch.id;

        v_count := v_count + 1;
    END LOOP;

    RETURN v_count;
END;
$$;

-- F5. Sinh booking slots
CREATE OR REPLACE FUNCTION fn_generate_slots(p_days INT DEFAULT 30)
RETURNS INT LANGUAGE plpgsql AS $$
DECLARE
    v_day   DATE; v_start TIME; v_end TIME;
    v_count INT; v_total INT := 0;
BEGIN
    FOR v_day IN
        SELECT gs::DATE FROM generate_series(
            CURRENT_DATE, CURRENT_DATE + p_days - 1, '1 day') gs
    LOOP
        v_start := '07:00'::TIME;
        WHILE v_start < '20:00'::TIME LOOP
            v_end := v_start + INTERVAL '30 minutes';
            INSERT INTO booking_slots (slot_date, slot_start, slot_end, capacity)
            VALUES (v_day, v_start, v_end, 3)
            ON CONFLICT (slot_date, slot_start) DO NOTHING;
            GET DIAGNOSTICS v_count = ROW_COUNT;
            v_total := v_total + v_count;
            v_start := v_end;
        END LOOP;
    END LOOP;
    RETURN v_total;
END;
$$;