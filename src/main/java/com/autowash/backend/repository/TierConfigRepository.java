package com.autowash.backend.repository;

import com.autowash.backend.entity.TierConfig;
import com.autowash.backend.enums.LoyaltyTier;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * PK is the LoyaltyTier enum itself - findById(LoyaltyTier) is all most
 * callers need (used throughout LoyaltyServiceImpl since Week 1).
 */
public interface TierConfigRepository extends JpaRepository<TierConfig, LoyaltyTier> {
}
