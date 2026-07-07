package com.autowash.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** A03 - response of POST /auth/refresh: { "access_token", "expires_in" } */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenRefreshResponse {
    private String accessToken;
    private long expiresIn;
}
