package com.autowash.autowash_pro.dto.response.loyalty;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EarnPointsResponse {
    private UUID customerId;
    private int pointsEarned;
    private int newBalance;
    private String newTier;
    private String message;
}
