package com.autowash.backend.service.impl;

import com.autowash.backend.common.util.PostgresErrorTranslator;
import com.autowash.backend.dto.request.LoginRequest;
import com.autowash.backend.dto.request.RefreshTokenRequest;
import com.autowash.backend.dto.request.RegisterRequest;
import com.autowash.backend.dto.response.AuthResponse;
import com.autowash.backend.dto.response.TokenRefreshResponse;
import com.autowash.backend.entity.LoyaltyAccount;
import com.autowash.backend.entity.RefreshToken;
import com.autowash.backend.entity.TierConfig;
import com.autowash.backend.entity.User;
import com.autowash.backend.enums.LoyaltyTier;
import com.autowash.backend.enums.UserRole;
import com.autowash.backend.exception.BusinessException;
import com.autowash.backend.exception.ErrorCode;
import com.autowash.backend.repository.LoyaltyAccountRepository;
import com.autowash.backend.repository.RefreshTokenRepository;
import com.autowash.backend.repository.TierConfigRepository;
import com.autowash.backend.repository.UserRepository;
import com.autowash.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

/**
 * A01-A04: register, login, refresh, logout.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.autowash.backend.security.JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LoyaltyAccountRepository loyaltyAccountRepository;
    private final TierConfigRepository tierConfigRepository;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new BusinessException(ErrorCode.PHONE_ALREADY_EXISTS, "Số điện thoại đã được đăng ký");
        }

        User user = User.builder()
                .phone(request.getPhone())
                .email(request.getEmail())
                .fullName(request.getFullName())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.CUSTOMER)
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .occupation(request.getOccupation())
                .acquisitionChannel(request.getAcquisitionChannel())
                .build();

        // D03 (Giai đoạn 3): pre-check above (existsByPhone) has a race window under
        // real concurrent traffic - two requests can both pass it before either
        // commits. The DB's uq_users_phone/uq_users_email constraints are the real
        // safety net; without this catch, the loser of that race got a raw 500
        // instead of PHONE_ALREADY_EXISTS.
        try {
            userRepository.save(user);
        } catch (org.springframework.dao.DataAccessException ex) {
            throw PostgresErrorTranslator.translate(ex);
        }

        // fn_init_loyalty() (DB trigger, AFTER INSERT ON users) has already created
        // the loyalty_accounts row synchronously as part of the INSERT above - safe
        // to read it back immediately within the same transaction.
        LoyaltyAccount account = loyaltyAccountRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalStateException(
                        "fn_init_loyalty() did not create a loyalty account for new user " + user.getId()));
        TierConfig tierConfig = tierConfigRepository.findById(account.getCurrentTier())
                .orElseThrow(() -> new IllegalStateException("Missing tier_configs row for " + account.getCurrentTier()));

        log.info("User {} registered with phone {}", user.getId(), user.getPhone());
        return buildAuthResponse(user, account, tierConfig);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS,
                        "Số điện thoại hoặc mật khẩu không đúng"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "Số điện thoại hoặc mật khẩu không đúng");
        }
        if (!user.isActive()) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "Tài khoản đã bị vô hiệu hoá");
        }

        LoyaltyAccount account = loyaltyAccountRepository.findByUserId(user.getId()).orElse(null);
        TierConfig tierConfig = account != null
                ? tierConfigRepository.findById(account.getCurrentTier()).orElse(null)
                : null;

        log.info("User {} logged in", user.getId());
        return buildAuthResponse(user, account, tierConfig);
    }

    @Override
    @Transactional
    public TokenRefreshResponse refresh(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenAndRevokedFalse(request.getRefreshToken())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN,
                        "Refresh token không hợp lệ hoặc đã bị thu hồi"));

        if (refreshToken.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN, "Refresh token đã hết hạn");
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(refreshToken.getUser());

        return TokenRefreshResponse.builder()
                .accessToken(newAccessToken)
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationMs() / 1000)
                .build();
    }

    @Override
    @Transactional
    public void logout(java.util.UUID userId) {
        int revoked = refreshTokenRepository.revokeAllForUser(userId);
        log.info("User {} logged out - {} refresh token(s) revoked", userId, revoked);
    }

    private AuthResponse buildAuthResponse(User user, LoyaltyAccount account, TierConfig tierConfig) {
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshTokenValue = jwtTokenProvider.generateRefreshToken(user);

        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .user(user)
                .token(refreshTokenValue)
                .expiresAt(OffsetDateTime.now().plus(jwtTokenProvider.getRefreshTokenExpirationMs(), ChronoUnit.MILLIS))
                .build();
        refreshTokenRepository.save(refreshTokenEntity);

        return AuthResponse.builder()
                .user(AuthResponse.UserPreview.builder()
                        .id(user.getId())
                        .fullName(user.getFullName())
                        .role(user.getRole())
                        .build())
                .loyalty(account != null
                        ? AuthResponse.LoyaltyPreview.builder()
                                .tier(account.getCurrentTier())
                                .pointsBalance(account.getPointsBalance())
                                .bookingWindowDays(tierConfig != null ? tierConfig.getBookingWindowDays() : null)
                                .build()
                        : AuthResponse.LoyaltyPreview.builder().tier(LoyaltyTier.MEMBER).pointsBalance(0).build())
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationMs() / 1000)
                .build();
    }
}
