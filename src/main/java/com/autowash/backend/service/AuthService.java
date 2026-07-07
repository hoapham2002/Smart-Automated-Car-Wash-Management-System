package com.autowash.backend.service;

import com.autowash.backend.dto.request.LoginRequest;
import com.autowash.backend.dto.request.RefreshTokenRequest;
import com.autowash.backend.dto.request.RegisterRequest;
import com.autowash.backend.dto.response.AuthResponse;
import com.autowash.backend.dto.response.TokenRefreshResponse;

import java.util.UUID;

/**
 * Backs A01-A04: register, login, refresh, logout.
 */
public interface AuthService {

    /** A01 - POST /auth/register */
    AuthResponse register(RegisterRequest request);

    /** A02 - POST /auth/login */
    AuthResponse login(LoginRequest request);

    /** A03 - POST /auth/refresh */
    TokenRefreshResponse refresh(RefreshTokenRequest request);

    /** A04 - POST /auth/logout - revokes every active refresh token for the user. */
    void logout(UUID userId);
}
