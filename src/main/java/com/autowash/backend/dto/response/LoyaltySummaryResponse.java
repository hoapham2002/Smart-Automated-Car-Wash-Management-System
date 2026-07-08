package com.autowash.backend.dto.response;

import com.autowash.backend.enums.LoyaltyTier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * B01 - GET /me/loyalty (full dashboard).
 * Also reused (subset) for the `loyalty` block returned inline by
 * /auth/register, /auth/login and GET /me.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltySummaryResponse {
    private LoyaltyTier tier;
    private Integer pointsBalance;
    private Integer pointsYtd;
    private Integer totalVisits;
    private BigDecimal totalSpend;
    private Integer streakDays;
    private LocalDate nextReviewAt;
    private Integer pointsToNextTier;
    private LoyaltyTier nextTier;

    private TierBenefits tierBenefits;

    /**
     * Left as an empty list for B01 - populated once point_expiry_batches is
     * wired up in Giai đoạn 2 (C05 - point expiry warning, owned by B).
     */
    @Builder.Default
    private List<ExpiringPoints> expiringPoints = List.of();

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TierBenefits {
        private Integer bookingWindowDays;
        private Integer queuePriority;
        private BigDecimal pointMultiplier;
        private Integer birthdayBonusPts;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExpiringPoints {
        private Integer points;
        private LocalDate expiresAt;
    }
}
