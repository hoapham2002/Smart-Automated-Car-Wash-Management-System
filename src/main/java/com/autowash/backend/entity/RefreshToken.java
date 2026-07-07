package com.autowash.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Maps to table `refresh_tokens`.
 *
 * ============================================================================
 *  ⚠️ SCHEMA GAP: this table does NOT exist in V1__init_schema.sql /
 *  V2-V4 as originally provided - the SQL schema document covers Loyalty/
 *  Booking/Payment/Research but never defined refresh token storage, even
 *  though A03 ("Implement POST /auth/refresh + lưu RefreshToken vào DB")
 *  explicitly requires persisting it. A new migration is required:
 *
 *  CREATE TABLE refresh_tokens (
 *      id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
 *      user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 *      token       TEXT NOT NULL,
 *      expires_at  TIMESTAMPTZ NOT NULL,
 *      revoked     BOOLEAN NOT NULL DEFAULT FALSE,
 *      created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
 *      CONSTRAINT uq_refresh_token UNIQUE (token)
 *  );
 *  CREATE INDEX idx_refresh_token_user ON refresh_tokens(user_id) WHERE revoked = FALSE;
 *
 *  Add this as V5__add_refresh_tokens.sql (see GIAIDOAN1_TUAN1_README.md).
 * ============================================================================
 */
@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token", nullable = false, unique = true, columnDefinition = "TEXT")
    private String token;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "revoked", nullable = false)
    @Builder.Default
    private boolean revoked = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
