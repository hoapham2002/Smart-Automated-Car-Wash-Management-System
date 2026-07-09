package com.autowash.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * A07 - GET /services?vehicle_size=
 * `price` is null when the caller doesn't pass vehicle_size (there's no
 * single "the" price - it varies per vehicle size, see service_prices).
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceResponse {
    private UUID id;
    private String name;
    private String description;
    private Integer durationMin;
    private Integer basePoints;
    private BigDecimal price;
}
