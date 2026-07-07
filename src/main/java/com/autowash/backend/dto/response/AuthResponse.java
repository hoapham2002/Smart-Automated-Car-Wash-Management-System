package com.autowash.backend.dto.response;

import com.autowash.backend.enums.LoyaltyTier;
import com.autowash.backend.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * A01/A02 - response of POST /auth/register and POST /auth/login.
 * {
 *   "user": { "id", "full_name", "role" },
 *   "loyalty": { "tier", "points_balance", "booking_window_days" },
 *   "access_token", "refresh_token", "expires_in"
 * }
 * Deliberately a compact preview (not the full UserResponse/LoyaltySummaryResponse) -
 * matches the API doc example exactly, since a freshly-registered/logged-in
 * client doesn't need the full loyalty dashboard yet (that's GET /me/loyalty).
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private UserPreview user;
    private LoyaltyPreview loyalty;
    private String accessToken;
    private String refreshToken;
    private long expiresIn;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserPreview {
        private UUID id;
        private String fullName;
        private UserRole role;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoyaltyPreview {
        private LoyaltyTier tier;
        private Integer pointsBalance;
        private Integer bookingWindowDays;
    }
}
