package com.autowash.backend.dto.response;

import com.autowash.backend.enums.VehicleSize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** A07 - GET /services/:id/prices - one row per vehicle size. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServicePriceResponse {
    private VehicleSize vehicleSize;
    private BigDecimal price;
}
