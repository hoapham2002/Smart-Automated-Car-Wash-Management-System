package com.autowash.backend.entity;

import com.autowash.backend.enums.LoyaltyTier;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Maps to table `tier_configs`. Admin-tunable (B04 - PATCH /admin/tier-configs/:tier).
 * The primary key IS the tier itself (no separate UUID id).
 */
@Entity
@Table(name = "tier_configs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TierConfig {

    @Id
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "tier", columnDefinition = "loyalty_tier")
    private LoyaltyTier tier;

    @Column(name = "min_points", nullable = false)
    private Integer minPoints;

    @Column(name = "min_visits", nullable = false)
    @Builder.Default
    private Integer minVisits = 0;

    @Column(name = "booking_window_days", nullable = false)
    private Integer bookingWindowDays;

    @Column(name = "queue_priority", nullable = false)
    private Integer queuePriority;

    @Column(name = "point_multiplier", nullable = false, precision = 4, scale = 2)
    @Builder.Default
    private BigDecimal pointMultiplier = BigDecimal.ONE;

    @Column(name = "birthday_bonus_pts", nullable = false)
    @Builder.Default
    private Integer birthdayBonusPts = 0;

    @Column(name = "free_wash_threshold")
    private Integer freeWashThreshold;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private OffsetDateTime updatedAt = OffsetDateTime.now();
}
