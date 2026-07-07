package com.autowash.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/** A02 - POST /auth/login */
@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "phone không được để trống")
    private String phone;

    @NotBlank(message = "password không được để trống")
    private String password;
}
