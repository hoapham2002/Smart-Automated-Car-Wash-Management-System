package com.autowash.backend.repository;

import com.autowash.backend.entity.LoyaltyAccount;
import com.autowash.backend.enums.LoyaltyTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Basic CRUD comes from JpaRepository; findByUserId(UUID) resolves via
 * nested property traversal against LoyaltyAccount.user (see that entity's
 * javadoc). Methods were added incrementally as each feature needed them:
 *   - Week 4 (A19): getTierDistribution() - full member count + avg spend,
 *     used by the admin dashboard's tier-distribution widget.
 *   - Week 1 (B05): countByCurrentTier(tier) - the simpler per-tier count
 *     named explicitly in the task table ("LoyaltyAccountRepository.countByTier()").
 * Both coexist fine; AdminTierConfigController (B) can use either depending
 * on whether it needs just counts or counts+avgSpend.
 */
public interface LoyaltyAccountRepository extends JpaRepository<LoyaltyAccount, UUID> {

    Optional<LoyaltyAccount> findByUserId(UUID userId);

    /** B05 - simple per-tier member count, as literally named in the task table. */
    long countByCurrentTier(LoyaltyTier tier);

    /**
     * A19 - member count + average spend per tier, used by the admin dashboard's
     * tier-distribution widget (DashboardServiceImpl.getTierDistribution()).
     */
    @Query("""
            SELECT la.currentTier AS tier,
                   COUNT(la)      AS memberCount,
                   COALESCE(AVG(la.totalSpend), 0) AS avgSpend
            FROM LoyaltyAccount la
            GROUP BY la.currentTier
            """)
    List<TierDistributionProjection> getTierDistribution();

    interface TierDistributionProjection {
        LoyaltyTier getTier();
        Long getMemberCount();
        BigDecimal getAvgSpend();
    }
}
