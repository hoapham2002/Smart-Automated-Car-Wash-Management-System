package com.autowash.backend.entity;

import com.autowash.backend.enums.LoyaltyTier;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Maps to table `loyalty_accounts`. Auto-created by the DB trigger
 * fn_init_loyalty() whenever a `customer` row is inserted into `users` -
 * Java code should never INSERT this entity directly, only read/UPDATE it.
 *
 * NOTE: the association is named `user` (not a flat `userId` field) so that
 * Spring Data's `findByUserId(UUID)` derived query - used throughout the
 * Loyalty/Promotion/Survey services since Week 1 - resolves via nested
 * property traversal (user.id) rather than needing a literal `userId` column.
 */
@Entity
@Table(name = "loyalty_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "current_tier", nullable = false, columnDefinition = "loyalty_tier")
    @Builder.Default
    private LoyaltyTier currentTier = LoyaltyTier.MEMBER;

    @Column(name = "points_balance", nullable = false)
    @Builder.Default
    private Integer pointsBalance = 0;

    @Column(name = "points_ytd", nullable = false)
    @Builder.Default
    private Integer pointsYtd = 0;

    @Column(name = "total_points_earned", nullable = false)
    @Builder.Default
    private Integer totalPointsEarned = 0;

    @Column(name = "total_points_spent", nullable = false)
    @Builder.Default
    private Integer totalPointsSpent = 0;

    @Column(name = "total_visits", nullable = false)
    @Builder.Default
    private Integer totalVisits = 0;

    @Column(name = "total_spend", nullable = false, precision = 14, scale = 0)
    @Builder.Default
    private BigDecimal totalSpend = BigDecimal.ZERO;

    @Column(name = "tier_achieved_at")
    private OffsetDateTime tierAchievedAt;

    /** Date of the next monthly tier review - see fn_review_tiers() / TierReviewJob. */
    @Column(name = "next_review_at")
    private LocalDate nextReviewAt;

    @Column(name = "last_visit_at")
    private OffsetDateTime lastVisitAt;

    @Column(name = "streak_days", nullable = false)
    @Builder.Default
    private Integer streakDays = 0;

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private OffsetDateTime updatedAt = OffsetDateTime.now();
}
