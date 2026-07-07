package com.autowash.backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * The single exception type every service in this project throws for
 * expected/documented business-rule failures (as opposed to bugs). Carries a
 * machine-readable errorCode (matching the ErrorCode constants, or the raw
 * strings a DB function's JSONB response uses, e.g. fn_redeem_points()'s
 * "error" field) plus a human-readable Vietnamese message.
 *
 * GlobalExceptionHandler catches this and renders:
 * { "success": false, "error": "<errorCode>", "message": "<message>" }
 */
@Getter
public class BusinessException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus httpStatus;

    public BusinessException(String errorCode, String message) {
        this(errorCode, message, defaultStatusFor(errorCode));
    }

    public BusinessException(String errorCode, String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    /**
     * Best-effort HTTP status inference from the error code, so most call
     * sites can just use the 2-arg constructor. Matches the specific status
     * codes documented in the API doc; anything unrecognized defaults to 400
     * (the large majority of business-rule violations are client errors).
     */
    private static HttpStatus defaultStatusFor(String errorCode) {
        return switch (errorCode) {
            case ErrorCode.INVALID_CREDENTIALS -> HttpStatus.UNAUTHORIZED;
            case ErrorCode.USER_NOT_FOUND, ErrorCode.VEHICLE_NOT_FOUND, ErrorCode.SESSION_NOT_FOUND,
                    ErrorCode.INVOICE_NOT_FOUND, ErrorCode.LOYALTY_ACCOUNT_NOT_FOUND,
                    ErrorCode.TIER_CONFIG_NOT_FOUND, ErrorCode.PROMOTION_NOT_FOUND,
                    ErrorCode.STAFF_NOT_FOUND, ErrorCode.SLOT_NOT_FOUND, ErrorCode.OPTION_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case ErrorCode.PLATE_ALREADY_EXISTS, ErrorCode.PHONE_ALREADY_EXISTS -> HttpStatus.CONFLICT;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}
