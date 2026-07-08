package com.autowash.backend.dto.request;

import com.autowash.backend.enums.VehicleSize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/** A05 - POST /me/vehicles */
@Getter
@Setter
public class CreateVehicleRequest {

    @NotBlank(message = "plate_number không được để trống")
    private String plateNumber;

    @NotNull(message = "vehicle_size không được để trống")
    private VehicleSize vehicleSize;

    private String brand;
    private String model;
    private String color;
    private boolean isPrimary;
}
