package com.autowash.backend.exception;

/**
 * Centralised error codes used across the Wash/Payment (Person A) and
 * Loyalty/Promotion (Person B) domains.
 * Pairs with the existing BusinessException(String errorCode, String message)
 * from Week 1, which GlobalExceptionHandler turns into:
 * { "success": false, "error": "<code>", "message": "<message>" }
 */
public final class ErrorCode {

    private ErrorCode() {
    }

    // Wash
    public static final String SESSION_NOT_FOUND = "SESSION_NOT_FOUND";
    public static final String INVALID_WASH_STATUS = "INVALID_WASH_STATUS";
    public static final String STAFF_NOT_FOUND = "STAFF_NOT_FOUND";
    public static final String NOT_YOUR_BOOKING = "NOT_YOUR_BOOKING";
    public static final String ALREADY_RATED = "ALREADY_RATED";
    public static final String NOT_DONE_YET = "NOT_DONE_YET";

    // Payment
    public static final String INVOICE_NOT_FOUND = "INVOICE_NOT_FOUND";
    public static final String ALREADY_PAID = "ALREADY_PAID";
    public static final String INVOICE_CANCELLED = "INVOICE_CANCELLED";
    public static final String INVALID_AMOUNT = "INVALID_AMOUNT";

    // Loyalty
    public static final String LOYALTY_ACCOUNT_NOT_FOUND = "LOYALTY_ACCOUNT_NOT_FOUND";
    public static final String TIER_CONFIG_NOT_FOUND = "TIER_CONFIG_NOT_FOUND";
    public static final String INVALID_TX_TYPE = "INVALID_TX_TYPE";

    // Redeem (mirrors the error strings fn_redeem_points() returns in its JSONB result)
    public static final String OPTION_NOT_FOUND = "OPTION_NOT_FOUND";
    public static final String TIER_TOO_LOW = "TIER_TOO_LOW";
    public static final String INSUFFICIENT_POINTS = "INSUFFICIENT_POINTS";

    // Promotion
    public static final String PROMOTION_NOT_FOUND = "PROMOTION_NOT_FOUND";
    public static final String INVALID_PROMOTION_FIELDS = "INVALID_PROMOTION_FIELDS";

    // Booking rules (C02 - mirrors the ERRCODEs raised by trg_check_booking_rules)
    public static final String SLOT_FULL = "SLOT_FULL";
    public static final String SLOT_NOT_FOUND = "SLOT_NOT_FOUND";
    public static final String BOOKING_WINDOW_EXCEEDED = "BOOKING_WINDOW_EXCEEDED";

    // Wash queue (C01)
    public static final String NO_SESSION_IN_QUEUE = "NO_SESSION_IN_QUEUE";

    // Auth (A01-A04)
    public static final String PHONE_ALREADY_EXISTS = "PHONE_ALREADY_EXISTS";
    public static final String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
    public static final String INVALID_REFRESH_TOKEN = "INVALID_REFRESH_TOKEN";
    public static final String USER_NOT_FOUND = "USER_NOT_FOUND";

    // Vehicle (A05-A06)
    public static final String PLATE_ALREADY_EXISTS = "PLATE_ALREADY_EXISTS";
    public static final String VEHICLE_NOT_FOUND = "VEHICLE_NOT_FOUND";
}
