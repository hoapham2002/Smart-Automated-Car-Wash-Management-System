package com.autowash.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/** A03 - POST /auth/refresh */
@Getter
@Setter
public class RefreshTokenRequest {

    @NotBlank(message = "refresh_token không được để trống")
    private String refreshToken;
}
