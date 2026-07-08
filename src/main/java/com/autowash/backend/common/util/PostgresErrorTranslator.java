package com.autowash.backend.common.util;

import com.autowash.backend.exception.BusinessException;
import com.autowash.backend.exception.ErrorCode;
import org.springframework.dao.DataAccessException;

import java.sql.SQLException;

/**
 * C02 - translates the custom Postgres SQLSTATE codes raised by
 * trg_check_booking_rules (see fn_check_booking_rules() in the schema) into
 * clean, documented BusinessExceptions instead of letting a raw
 * DataAccessException/PSQLException bubble up as a generic 500.
 *
 * fn_check_booking_rules() raises:
 *   - ERRCODE 'P0001' -> slot không tồn tại hoặc đang bị khoá
 *   - ERRCODE 'P0002' -> slot đã hết chỗ            (API doc: SLOT_FULL)
 *   - ERRCODE 'P0010' -> vượt quá booking window     (API doc: BOOKING_WINDOW_EXCEEDED)
 *
 * D03 (production bug, Giai đoạn 3): under real concurrent traffic, TWO
 * requests can both pass fn_check_booking_rules()'s capacity check (it does
 * a plain SELECT, no FOR UPDATE row lock) for the last remaining slot, then
 * both proceed to INSERT. The actual overbooking guard in that race is the
 * table-level CHECK constraint `chk_no_overbook` on booking_slots, fired by
 * trg_slot_count's UPDATE - which raises a generic Postgres check_violation
 * (SQLSTATE 23514), NOT one of our custom P00xx codes. Before this fix, that
 * meant the unlucky second customer saw a raw 500 "Đã có lỗi xảy ra" instead
 * of a proper SLOT_FULL message - confusing, and indistinguishable from an
 * actual server error. Now handled explicitly below.
 *
 * USAGE (in BookingServiceImpl.create(), wrap the repository.save(booking) call):
 * <pre>{@code
 * try {
 *     bookingRepository.save(booking);
 * } catch (DataAccessException ex) {
 *     throw PostgresErrorTranslator.translate(ex);
 * }
 * }</pre>
 * This is intentionally a static utility (not a @RestControllerAdvice) so it
 * doesn't risk colliding with whatever @ExceptionHandler(DataAccessException...)
 * mapping may already exist in GlobalExceptionHandler (Sprint 0) - the calling
 * service decides exactly where to apply it.
 */
public final class PostgresErrorTranslator {

    private static final String CHECK_VIOLATION = "23514";
    private static final String UNIQUE_VIOLATION = "23505";

    private PostgresErrorTranslator() {
    }

    public static BusinessException translate(DataAccessException ex) {
        String sqlState = extractSqlState(ex);

        if (sqlState == null) {
            return new BusinessException("BOOKING_RULE_VIOLATION",
                    "Yêu cầu đặt lịch không hợp lệ: " + rootMessage(ex));
        }

        return switch (sqlState) {
            case "P0002" -> new BusinessException(ErrorCode.SLOT_FULL, "Slot đã hết chỗ, vui lòng chọn slot khác");
            case "P0001" -> new BusinessException(ErrorCode.SLOT_NOT_FOUND, "Slot không tồn tại hoặc đang bị khoá");
            case "P0010" -> new BusinessException(ErrorCode.BOOKING_WINDOW_EXCEEDED,
                    extractPgMessageOrDefault(ex,
                            "Tier hiện tại của bạn chỉ được đặt lịch trước trong một số ngày giới hạn"));
            case CHECK_VIOLATION -> translateCheckViolation(ex);
            case UNIQUE_VIOLATION -> translateUniqueViolation(ex);
            default -> new BusinessException("BOOKING_RULE_VIOLATION",
                    "Yêu cầu đặt lịch không hợp lệ: " + rootMessage(ex));
        };
    }

    /** D03 fix - see the class javadoc's race-condition note. */
    private static BusinessException translateCheckViolation(DataAccessException ex) {
        String message = extractPgMessageOrDefault(ex, "");
        if (message.contains("chk_no_overbook")) {
            return new BusinessException(ErrorCode.SLOT_FULL,
                    "Slot vừa hết chỗ (có khách khác vừa đặt cùng lúc), vui lòng chọn slot khác");
        }
        return new BusinessException("CONSTRAINT_VIOLATION", "Dữ liệu không hợp lệ: " + rootMessage(ex));
    }

    /**
     * D03 fix - a second realistic concurrency bug: two simultaneous
     * /auth/register calls with the same phone/email both pass the
     * pre-insert existsByPhone()/existsByEmail() check before either commits,
     * then race to INSERT - the loser hits users.uq_users_phone or
     * uq_users_email as a raw unique_violation instead of a clean
     * PHONE_ALREADY_EXISTS/duplicate-email message.
     */
    private static BusinessException translateUniqueViolation(DataAccessException ex) {
        String message = extractPgMessageOrDefault(ex, "");
        if (message.contains("uq_users_phone")) {
            return new BusinessException(ErrorCode.PHONE_ALREADY_EXISTS, "Số điện thoại đã được đăng ký");
        }
        if (message.contains("uq_users_email")) {
            return new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS, "Email đã được đăng ký");
        }
        if (message.contains("uq_vehicle_plate")) {
            return new BusinessException(ErrorCode.PLATE_ALREADY_EXISTS, "Biển số này đã được đăng ký");
        }
        return new BusinessException("DUPLICATE_VALUE", "Dữ liệu đã tồn tại: " + rootMessage(ex));
    }

    private static String extractSqlState(DataAccessException ex) {
        Throwable cause = ex;
        while (cause != null) {
            if (cause instanceof SQLException sqlEx) {
                return sqlEx.getSQLState();
            }
            cause = cause.getCause();
        }
        return null;
    }

    /**
     * fn_check_booking_rules() raises a rich, already-Vietnamese, human-readable
     * message for P0010 (includes the customer's tier + allowed window + how many
     * days ahead they tried to book) - prefer surfacing that verbatim over our own
     * generic fallback when we can find it.
     */
    private static String extractPgMessageOrDefault(DataAccessException ex, String fallback) {
        Throwable cause = ex;
        while (cause != null) {
            if (cause instanceof SQLException sqlEx && sqlEx.getMessage() != null) {
                return sqlEx.getMessage();
            }
            cause = cause.getCause();
        }
        return fallback;
    }

    private static String rootMessage(DataAccessException ex) {
        Throwable root = ex;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        return root.getMessage() != null ? root.getMessage() : ex.getMessage();
    }
}
