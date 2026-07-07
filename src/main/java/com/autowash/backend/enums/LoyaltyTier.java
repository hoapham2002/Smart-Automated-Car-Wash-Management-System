package com.autowash.backend.enums;

/**
 * Mirrors the Postgres enum `loyalty_tier`: ('member','silver','gold','platinum').
 *
 * IMPORTANT: declaration order is load-bearing. Several places in the codebase
 * rely on ordinal() for tier ranking/comparison and on values()[i+1] to find
 * "the next tier up" (e.g. LoyaltyServiceImpl.nextTierOf(),
 * getRedemptionOptions()'s tier-eligibility check). This order must always
 * match ascending min_points from tier_configs (member=0 < silver=500 <
 * gold=2000 < platinum=5000).
 */
public enum LoyaltyTier {
    MEMBER,
    SILVER,
    GOLD,
    PLATINUM
}
