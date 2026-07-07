package com.autowash.backend.controller;

import com.autowash.backend.common.response.ApiResponse;
import com.autowash.backend.dto.request.LoginRequest;
import com.autowash.backend.dto.request.RefreshTokenRequest;
import com.autowash.backend.dto.request.RegisterRequest;
import com.autowash.backend.dto.response.AuthResponse;
import com.autowash.backend.dto.response.TokenRefreshResponse;
import com.autowash.backend.security.SecurityUtils;
import com.autowash.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * A01-A04: register, login, refresh, logout.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Register, login, token refresh, logout")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Đăng ký tài khoản mới")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(authService.register(request)));
    }

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.login(request)));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Làm mới access token bằng refresh token")
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.refresh(request)));
    }

    @PostMapping("/logout")
    @Operation(summary = "Đăng xuất - thu hồi mọi refresh token đang hoạt động")
    public ResponseEntity<ApiResponse<Void>> logout() {
        authService.logout(SecurityUtils.currentUserId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
