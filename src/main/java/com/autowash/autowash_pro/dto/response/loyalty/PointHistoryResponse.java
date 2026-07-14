package com.autowash.autowash_pro.dto.response.loyalty;

import java.time.LocalDateTime;
import java.util.UUID;

import com.autowash.autowash_pro.enums.PointType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PointHistoryResponse {
    private UUID pointId;
    private PointType type;
    private int points;
    private int balanceAfter;
    private String description;
    private UUID referenceId;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}
