package com.autowash.autowash_pro.dto.response.customer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.autowash.autowash_pro.enums.Tier;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerProfileResponse {
    private UUID customerId;
    private String fullName;
    private String phone;
    private String email;
    private Tier tier;
    
    // Loyalty points
    private int totalPoints;
    private int lifetimePoints;
    
    // Statistics
    private int totalVisits;
    private BigDecimal totalSpend;
    
    // Timestamps
    private LocalDateTime registeredAt;
    private LocalDateTime lastVisitAt;
    
    // Status
    private boolean isActive;

    public static CustomerProfileResponse from(com.autowash.autowash_pro.entity.Customer customer) {
        return CustomerProfileResponse.builder()
                .customerId(customer.getCustomerId())
                .fullName(customer.getFullName())
                .phone(customer.getPhone())
                .email(customer.getEmail())
                .tier(customer.getTier())
                .totalPoints(customer.getTotalPoints())
                .lifetimePoints(customer.getLifetimePoints())
                .totalVisits(customer.getTotalVisits())
                .totalSpend(customer.getTotalSpend())
                .registeredAt(customer.getRegisteredAt())
                .lastVisitAt(customer.getLastVisitAt())
                .isActive(customer.isActive())
                .build();
    }
}
