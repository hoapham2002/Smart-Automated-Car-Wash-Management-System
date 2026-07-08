package com.autowash.backend.dto.response;

import com.autowash.backend.entity.Vehicle;
import com.autowash.backend.enums.VehicleSize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/** A05/A06 - shape returned by all /me/vehicles endpoints. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleResponse {
    private UUID id;
    private String plateNumber;
    private VehicleSize vehicleSize;
    private String brand;
    private String model;
    private String color;
    private boolean isPrimary;

    public static VehicleResponse from(Vehicle v) {
        return VehicleResponse.builder()
                .id(v.getId())
                .plateNumber(v.getPlateNumber())
                .vehicleSize(v.getVehicleSize())
                .brand(v.getBrand())
                .model(v.getModel())
                .color(v.getColor())
                .isPrimary(v.isPrimary())
                .build();
    }
}
