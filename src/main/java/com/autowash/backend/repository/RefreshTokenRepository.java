package com.autowash.backend.repository;

import com.autowash.backend.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    /** A03 - GET the token row by its raw value to validate/rotate on refresh. */
    Optional<RefreshToken> findByTokenAndRevokedFalse(String token);

    Optional<RefreshToken> findByToken(String token);

    /**
     * A04 - POST /auth/logout + xoá RefreshToken. Revokes (soft-delete) every
     * active token for the user rather than a single session's token, since
     * this app doesn't track per-device session identifiers - see
     * AuthServiceImpl.logout() and GIAIDOAN1_TUAN1_README.md for the
     * single-vs-multi-session tradeoff this implies.
     */
    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user.id = :userId AND rt.revoked = false")
    int revokeAllForUser(@Param("userId") UUID userId);
}
