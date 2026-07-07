-- ============================================================
--  AutoWash Pro – V3: Views
-- ============================================================

-- ── V1. v_customer_loyalty ───────────────────────────────
-- Dùng cho: GET /me/loyalty, GET /me (loyalty summary)
CREATE VIEW v_customer_loyalty AS
SELECT
    u.id,
    u.full_name,
    u.phone,
    u.email,
    u.occupation,
    u.gender,
    la.current_tier,
    la.points_balance,
    la.points_ytd,
    la.total_visits,
    la.total_spend,
    la.last_visit_at,
    la.streak_days,
    la.next_review_at,
    tc.booking_window_days,
    tc.queue_priority,
    tc.point_multiplier,
    tc.free_wash_threshold,
    tc.birthday_bonus_pts,
    -- Điểm còn thiếu để lên tier tiếp theo
    CASE la.current_tier
        WHEN 'MEMBER'   THEN GREATEST(500  - la.points_ytd, 0)
        WHEN 'SILVER'   THEN GREATEST(2000 - la.points_ytd, 0)
        WHEN 'GOLD'     THEN GREATEST(5000 - la.points_ytd, 0)
        ELSE NULL
    END AS points_to_next_tier,
    -- Tên tier tiếp theo
    CASE la.current_tier
        WHEN 'MEMBER'   THEN 'SILVER'
        WHEN 'SILVER'   THEN 'GOLD'
        WHEN 'GOLD'     THEN 'PLATINUM'
        ELSE NULL
    END AS next_tier
FROM users u
JOIN loyalty_accounts la ON la.user_id = u.id
JOIN tier_configs     tc ON tc.tier    = la.current_tier
WHERE u.role = 'CUSTOMER';

-- ── V2. v_wash_queue ─────────────────────────────────────
-- Dùng cho: GET /wash/queue (staff/admin màn hình xưởng)
-- Sắp xếp: tier cao → checked_in sớm → lên trước
CREATE VIEW v_wash_queue AS
SELECT
    ROW_NUMBER() OVER (
        ORDER BY bk.queue_priority DESC, bk.checked_in_at ASC
    )                                                    AS queue_pos,
    ws.id                                                AS session_id,
    ws.bay_number,
    ws.status                                            AS wash_status,
    bk.id                                                AS booking_id,
    bk.booking_code,
    bk.queue_priority,
    bk.tier_at_booking,
    bk.is_walkin,
    v.plate_number,
    v.vehicle_size,
    s.name                                               AS service_name,
    s.duration_min                                       AS est_min,
    u.full_name                                          AS customer_name,
    u.phone                                              AS customer_phone,
    la.current_tier,
    bk.checked_in_at,
    ws.started_at,
    ROUND(
        EXTRACT(EPOCH FROM NOW() - ws.started_at) / 60, 0
    )                                                    AS elapsed_min
FROM wash_sessions ws
JOIN bookings       bk ON bk.id      = ws.booking_id
JOIN vehicles       v  ON v.id       = bk.vehicle_id
JOIN services       s  ON s.id       = bk.service_id
JOIN users          u  ON u.id       = bk.customer_id
JOIN loyalty_accounts la ON la.user_id = u.id
WHERE ws.status NOT IN ('DONE')
ORDER BY queue_pos;

-- ── V3. v_admin_kpi ──────────────────────────────────────
-- Dùng cho: GET /admin/dashboard – KPI header
CREATE VIEW v_admin_kpi AS
SELECT
    COUNT(DISTINCT CASE
        WHEN b.scheduled_at::DATE = CURRENT_DATE
        THEN b.id END)                                   AS today_bookings,

    COUNT(DISTINCT CASE
        WHEN b.status = 'COMPLETED'
         AND b.completed_at::DATE = CURRENT_DATE
        THEN b.id END)                                   AS today_completed,

    COALESCE(SUM(CASE
        WHEN i.paid_at::DATE = CURRENT_DATE
         AND i.status = 'PAID'
        THEN i.total_amount END), 0)                     AS today_revenue,

    COUNT(DISTINCT CASE
        WHEN la.current_tier = 'PLATINUM'
        THEN la.user_id END)                             AS platinum_members,

    COUNT(DISTINCT CASE
        WHEN la.current_tier = 'GOLD'
        THEN la.user_id END)                             AS gold_members,

    COUNT(DISTINCT CASE
        WHEN la.current_tier = 'SILVER'
        THEN la.user_id END)                             AS silver_members,

    COUNT(DISTINCT CASE
        WHEN la.current_tier = 'MEMBER'
        THEN la.user_id END)                             AS member_count,

    COUNT(DISTINCT CASE
        WHEN ws.status NOT IN ('DONE')
        THEN ws.id END)                                  AS active_washes

FROM bookings b
LEFT JOIN invoices       i  ON i.booking_id  = b.id
LEFT JOIN loyalty_accounts la ON la.user_id  = b.customer_id
LEFT JOIN wash_sessions  ws ON ws.booking_id = b.id;

-- ── V4. v_daily_revenue ──────────────────────────────────
-- Dùng cho: GET /admin/revenue – biểu đồ doanh thu theo ngày
CREATE VIEW v_daily_revenue AS
SELECT
    DATE(bk.completed_at)                            AS revenue_date,
    COUNT(*)                                         AS total_bookings,
    SUM(i.total_amount)                              AS gross_revenue,
    SUM(i.discount_amount)                           AS total_discounts,
    SUM(i.total_amount - i.discount_amount)          AS net_revenue,
    ROUND(AVG(i.total_amount), 0)                    AS avg_order_value,
    COUNT(*) FILTER (WHERE p.method = 'CASH')        AS cash_count,
    COUNT(*) FILTER (WHERE p.method = 'TRANSFER')    AS transfer_count
FROM invoices i
JOIN bookings bk ON bk.id       = i.booking_id
LEFT JOIN payments p ON p.invoice_id = i.id
              AND p.status = 'PAID'
WHERE i.status        = 'PAID'
  AND bk.completed_at IS NOT NULL
GROUP BY DATE(bk.completed_at)
ORDER BY revenue_date DESC;

-- ── V5. v_research_dataset ───────────────────────────────
-- Dùng cho: GET /research/export – export data cho ML model
CREATE VIEW v_research_dataset AS
SELECT
    b.id                                             AS booking_id,
    b.scheduled_at,
    b.tier_at_booking,
    b.final_price,
    b.points_earned,
    b.points_redeemed,
    b.redemption_type,
    b.booking_channel,
    (b.used_promo_id IS NOT NULL)                    AS used_promo,
    -- Customer profile (demographics cho research)
    u.occupation,
    u.gender,
    EXTRACT(YEAR FROM AGE(u.date_of_birth))::INT     AS age,
    -- Loyalty state tại thời điểm booking
    la.total_visits,
    la.total_spend,
    la.points_ytd,
    la.streak_days,
    -- Service & vehicle
    s.name                                           AS service_name,
    s.duration_min,
    v.vehicle_size,
    -- Kết quả
    b.status,
    ws.customer_rating,
    ws.quality_score,
    -- Survey data (nếu user đã làm khảo sát)
    sr.q_loyalty_program_importance,
    sr.q_tier_upgrade_motivation,
    sr.q_price_sensitivity,
    sr.q_frequency_per_month,
    sr.q_referral_likelihood,
    sr.is_synthetic
FROM bookings b
JOIN users            u  ON u.id        = b.customer_id
JOIN loyalty_accounts la ON la.user_id  = u.id
JOIN services         s  ON s.id        = b.service_id
JOIN vehicles         v  ON v.id        = b.vehicle_id
LEFT JOIN wash_sessions  ws ON ws.booking_id = b.id
LEFT JOIN survey_responses sr ON sr.user_id  = u.id
WHERE b.status = 'COMPLETED';