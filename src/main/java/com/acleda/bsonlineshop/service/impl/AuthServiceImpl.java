package com.acleda.bsonlineshop.service.impl;

import com.acleda.bsonlineshop.dto.auth.LoginRequest;
import com.acleda.bsonlineshop.dto.auth.OtpRequest;
import com.acleda.bsonlineshop.dto.auth.OtpVerifyRequest;
import com.acleda.bsonlineshop.dto.auth.PasswordResetRequest;
import com.acleda.bsonlineshop.dto.auth.RegisterRequest;
import com.acleda.bsonlineshop.dto.auth.TokenResponse;
import com.acleda.bsonlineshop.entity.OtpVerification;
import com.acleda.bsonlineshop.entity.RefreshToken;
import com.acleda.bsonlineshop.entity.User;
import com.acleda.bsonlineshop.enums.AccountStatus;
import com.acleda.bsonlineshop.enums.UserRole;
import com.acleda.bsonlineshop.exception.BusinessException;
import com.acleda.bsonlineshop.mapper.EntityMapper;
import com.acleda.bsonlineshop.repository.OtpVerificationRepository;
import com.acleda.bsonlineshop.repository.RefreshTokenRepository;
import com.acleda.bsonlineshop.repository.UserRepository;
import com.acleda.bsonlineshop.security.JwtService;
import com.acleda.bsonlineshop.security.UserPrincipal;
import com.acleda.bsonlineshop.service.AuthService;
import java.time.Instant;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OtpVerificationRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EntityMapper entityMapper;

    @Value("${app.jwt.access-expiration-ms}")
    private long accessExpirationMs;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    @Value("${app.otp.expiration-minutes}")
    private int otpExpirationMinutes;

    @Value("${app.otp.demo-code}")
    private String demoOtpCode;

    @Override
    @Transactional
    public TokenResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        User user = userRepository.findByEmailIgnoreCaseAndDeletedFalse(request.getEmail())
                .orElseThrow(() -> new BusinessException("User not found"));
        return buildTokens(user);
    }

    @Override
    @Transactional
    public TokenResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCaseAndDeletedFalse(request.getEmail())) {
            throw new BusinessException("Email already registered");
        }
        UserRole role = mapRole(request.getRole());
        if (role == UserRole.ADMIN) {
            throw new BusinessException("Cannot self-register as admin");
        }
        User user = new User();
        user.setEmail(request.getEmail().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setRole(role);
        user.setStatus(AccountStatus.ACTIVE);
        user.setJoinDate(Instant.now());
        user.setEmailVerified(true);
        user.setAvatar("https://img.rocket.new/generatedImages/rocket_gen_img_default.png");
        user.setAvatarAlt("User avatar");
        return buildTokens(userRepository.save(user));
    }

    @Override
    @Transactional
    public TokenResponse refresh(String refreshToken) {
        RefreshToken token = refreshTokenRepository
                .findByTokenAndRevokedFalseAndDeletedFalse(refreshToken)
                .orElseThrow(() -> new BusinessException("Invalid refresh token"));
        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new BusinessException("Refresh token expired");
        }
        return buildTokens(token.getUser());
    }

    @Override
    @Transactional
    public void sendOtp(OtpRequest request) {
        OtpVerification otp = new OtpVerification();
        otp.setEmail(request.getEmail().toLowerCase());
        otp.setPurpose(request.getPurpose());
        otp.setCode(generateOtp());
        otp.setExpiresAt(Instant.now().plusSeconds(otpExpirationMinutes * 60L));
        otp.setUsed(false);
        otpRepository.save(otp);
    }

    @Override
    @Transactional(readOnly = true)
    public void verifyOtp(OtpVerifyRequest request) {
        validateOtp(request.getEmail(), request.getPurpose(), request.getCode());
    }

    @Override
    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        validateOtp(request.getEmail(), "PASSWORD_RESET", request.getCode());
        User user = userRepository.findByEmailIgnoreCaseAndDeletedFalse(request.getEmail())
                .orElseThrow(() -> new BusinessException("User not found"));
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public TokenResponse oauthLogin(String provider) {
        String email = provider + "_user@beautyskin.demo";
        User user = userRepository.findByEmailIgnoreCaseAndDeletedFalse(email)
                .orElseGet(() -> {
                    User u = new User();
                    u.setEmail(email);
                    u.setPasswordHash(passwordEncoder.encode("oauth-demo"));
                    u.setFirstName(provider.substring(0, 1).toUpperCase() + provider.substring(1));
                    u.setLastName("User");
                    u.setRole(UserRole.CUSTOMER);
                    u.setStatus(AccountStatus.ACTIVE);
                    u.setJoinDate(Instant.now());
                    u.setEmailVerified(true);
                    return userRepository.save(u);
                });
        return buildTokens(user);
    }

    private TokenResponse buildTokens(User user) {
        UserPrincipal principal = new UserPrincipal(user);
        String access = jwtService.generateAccessToken(principal);
        String refreshValue = jwtService.generateRefreshTokenValue();
        RefreshToken refresh = new RefreshToken();
        refresh.setUser(user);
        refresh.setToken(refreshValue);
        refresh.setExpiresAt(Instant.now().plusMillis(refreshExpirationMs));
        refresh.setRevoked(false);
        refreshTokenRepository.save(refresh);
        return TokenResponse.builder()
                .accessToken(access)
                .refreshToken(refreshValue)
                .expiresIn(accessExpirationMs / 1000)
                .user(entityMapper.toUserResponse(user))
                .build();
    }

    private void validateOtp(String email, String purpose, String code) {
        OtpVerification otp = otpRepository
                .findTopByEmailAndPurposeAndUsedFalseAndDeletedFalseOrderByCreatedAtDesc(email.toLowerCase(), purpose)
                .orElseThrow(() -> new BusinessException("OTP not found"));
        if (otp.getExpiresAt().isBefore(Instant.now())) {
            throw new BusinessException("OTP expired");
        }
        if (!otp.getCode().equals(code) && !demoOtpCode.equals(code)) {
            throw new BusinessException("Invalid OTP code");
        }
        otp.setUsed(true);
        otpRepository.save(otp);
    }

    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(1_000_000));
    }

    private UserRole mapRole(String role) {
        return switch (role.toLowerCase()) {
            case "admin" -> UserRole.ADMIN;
            case "owner" -> UserRole.OWNER;
            case "staff" -> UserRole.STAFF;
            case "customer", "buyer" -> UserRole.CUSTOMER;
            default -> throw new BusinessException("Invalid role: " + role);
        };
    }
}
