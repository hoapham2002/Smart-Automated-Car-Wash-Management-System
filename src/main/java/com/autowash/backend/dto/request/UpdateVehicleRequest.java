package com.autowash.backend.dto.request;

import lombok.Getter;
import lombok.Setter;

/**
 * A06 - PUT /me/vehicles/:id
 * All fields optional (partial update) - only non-null fields are applied,
 * same convention as UpdateTierConfigRequest (B04).
 */
@Getter
@Setter
public class UpdateVehicleRequest {
    private String plateNumber;
    private String brand;
    private String model;
    private String color;
    private Boolean isPrimary;
}
