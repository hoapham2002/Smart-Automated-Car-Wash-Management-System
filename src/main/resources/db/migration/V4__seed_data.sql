-- ============================================================
--  AutoWash Pro – V4: Seed Data
-- ============================================================

-- Services cho xe máy
INSERT INTO services (name, description, duration_min, base_points, sort_order) VALUES
    ('Rửa nhanh',          'Rửa ngoài + sấy khô',                    10, 5,  1),
    ('Rửa tiêu chuẩn',     'Rửa kỹ + lau sơ + vệ sinh mặt đồng hồ', 20, 10, 2),
    ('Rửa cao cấp',        'Rửa kỹ + wax + đánh bóng mâm',           35, 20, 3),
    ('Vệ sinh máy',        'Rửa buồng máy + tẩy dầu mỡ',             25, 15, 4),
    ('Combo toàn diện',    'Rửa cao cấp + Vệ sinh máy. Tiết kiệm 10%',50, 30, 5);

INSERT INTO service_prices (service_id, vehicle_size, price)
SELECT s.id, p.vsize::vehicle_size, p.price
FROM services s
JOIN (VALUES
    ('Rửa nhanh',       'motorbike',   25000),
    ('Rửa nhanh',       'scooter',     30000),
    ('Rửa nhanh',       'sport_bike',  35000),
    ('Rửa tiêu chuẩn',  'motorbike',   40000),
    ('Rửa tiêu chuẩn',  'scooter',     45000),
    ('Rửa tiêu chuẩn',  'sport_bike',  55000),
    ('Rửa cao cấp',     'motorbike',   70000),
    ('Rửa cao cấp',     'scooter',     80000),
    ('Rửa cao cấp',     'sport_bike',  95000),
    ('Vệ sinh máy',     'motorbike',   60000),
    ('Vệ sinh máy',     'scooter',     65000),
    ('Vệ sinh máy',     'sport_bike',  75000),
    ('Combo toàn diện', 'motorbike',  120000),
    ('Combo toàn diện', 'scooter',    135000),
    ('Combo toàn diện', 'sport_bike', 160000)
) AS p(svc_name, vsize, price) ON s.name = p.svc_name;

-- Redemption options
INSERT INTO redemption_options (name, description, redemption_type, points_required, discount_value, min_tier) VALUES
    ('Giảm 10,000đ',     'Giảm 10k vào lần rửa tiếp theo',  'discount',   100, 10000,  'member'),
    ('Giảm 25,000đ',     'Giảm 25k – dành cho Silver+',     'discount',   230, 25000,  'silver'),
    ('Giảm 50,000đ',     'Giảm 50k – dành cho Gold+',       'discount',   430, 50000,  'gold'),
    ('Rửa xe miễn phí',  'Đổi 1 lần rửa tiêu chuẩn',       'free_wash',  500, NULL,   'member'),
    ('Vệ sinh máy miễn phí', 'Đổi 1 lần vệ sinh máy',       'free_wash',  600, NULL,   'silver'),
    ('Combo toàn diện',  'Đổi 1 lần combo – Platinum only', 'free_wash', 1200, NULL,   'platinum');

-- Booking slots 30 ngày
SELECT fn_generate_slots(30);

-- Users mẫu (trigger tự tạo loyalty_account)
-- LƯU Ý: password_hash bên dưới là placeholder, KHÔNG phải BCrypt hash hợp lệ.
-- Trước khi login được qua AuthService thật, cần UPDATE lại bằng hash BCrypt
-- thật, ví dụ tạo qua: new BCryptPasswordEncoder().encode("YourPassword123")
INSERT INTO users (phone, email, full_name, password_hash, role, occupation, gender) VALUES
    ('0901000001','admin@autowash.vn','Admin Hệ Thống',  'hash_admin', 'admin',    NULL,          NULL),
    ('0901000010','staff1@autowash.vn','Lê Văn Tuấn',   'hash_staff', 'staff',    NULL,          'male'),
    ('0901000020','khoa@gmail.com', 'Trần Minh Khoa',   'hash_c1',    'customer', 'sinh viên',   'male'),
    ('0901000021','hoa@gmail.com',  'Lê Thị Hoa',       'hash_c2',    'customer', 'nhân viên',   'female'),
    ('0901000022','bao@gmail.com',  'Nguyễn Quốc Bảo',  'hash_c3',    'customer', 'sinh viên',   'male'),
    ('0901000023','ngoc@gmail.com', 'Phạm Thị Ngọc',    'hash_c4',    'customer', 'nhân viên',   'female');

INSERT INTO vehicles (owner_id, plate_number, plate_normalized, vehicle_size, brand, model, color, is_primary)
VALUES
    ((SELECT id FROM users WHERE phone='0901000020'), '59H1-12345','59H112345','motorbike','Honda','Wave Alpha','Đỏ',    TRUE),
    ((SELECT id FROM users WHERE phone='0901000021'), '51G1-67890','51G167890','scooter',  'Yamaha','Janus','Trắng',    TRUE),
    ((SELECT id FROM users WHERE phone='0901000022'), '51A1-11122','51A111122','sport_bike','Honda','Winner X','Đen',   TRUE),
    ((SELECT id FROM users WHERE phone='0901000023'), '29B1-33344','29B133344','scooter',  'Honda','Vision','Xanh',    TRUE);
