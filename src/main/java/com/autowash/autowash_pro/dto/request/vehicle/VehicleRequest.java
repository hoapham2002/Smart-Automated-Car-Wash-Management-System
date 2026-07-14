package com.autowash.autowash_pro.dto.request.vehicle;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VehicleRequest {

    @NotBlank(message = "Bien so xe khong duoc de trong")
    @Size(max = 20, message = "Bien so xe toi da 20 ky tu")
    private String licensePlate;

    @NotBlank(message = "Loai xe khong duoc de trong")
    @Size(max = 50, message = "Loai xe toi da 50 ky tu")
    private String vehicleType;

    @Size(max = 50, message = "Hang xe toi da 50 ky tu")
    private String brand;

    @Size(max = 30, message = "Mau xe toi da 30 ky tu")
    private String color;

    private Boolean primary;
}
