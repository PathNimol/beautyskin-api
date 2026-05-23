package com.acleda.bsonlineshop.service.impl;

import com.acleda.bsonlineshop.auth.OtpPurposes;
import com.acleda.bsonlineshop.dto.auth.LoginRequest;
import com.acleda.bsonlineshop.dto.auth.OtpRequest;
import com.acleda.bsonlineshop.dto.auth.OtpVerifyRequest;
import com.acleda.bsonlineshop.dto.auth.PasswordResetRequest;
import com.acleda.bsonlineshop.dto.auth.RegisterPendingResponse;
import com.acleda.bsonlineshop.dto.auth.RegisterRequest;
import com.acleda.bsonlineshop.dto.auth.RegisterVerifyRequest;
import com.acleda.bsonlineshop.dto.auth.TokenResponse;
import com.acleda.bsonlineshop.entity.OtpVerification;
import com.acleda.bsonlineshop.entity.RefreshToken;
import com.acleda.bsonlineshop.entity.User;
import com.acleda.bsonlineshop.enums.AccountStatus;
import com.acleda.bsonlineshop.enums.UserRole;
import com.acleda.bsonlineshop.exception.BusinessException;
import com.acleda.bsonlineshop.mapper.EntityMapper;
import com.acleda.bsonlineshop.notification.OtpMailService;
import com.acleda.bsonlineshop.repository.OtpVerificationRepository;
import com.acleda.bsonlineshop.repository.RefreshTokenRepository;
import com.acleda.bsonlineshop.repository.UserRepository;
import com.acleda.bsonlineshop.security.JwtService;
import com.acleda.bsonlineshop.security.SecurityUtils;
import com.acleda.bsonlineshop.security.UserPrincipal;
import com.acleda.bsonlineshop.service.AuthService;
import com.acleda.bsonlineshop.validation.PasswordPolicy;
import java.time.Instant;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
    private final OtpMailService otpMailService;

    @Value("${app.jwt.access-expiration-ms}")
    private long accessExpirationMs;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    @Value("${app.otp.expiration-minutes}")
    private int otpExpirationMinutes;

    @Value("${app.otp.demo-code:}")
    private String demoOtpCode;

    @Value("${app.auth.registration.require-delivered-email:false}")
    private boolean registrationRequireDeliveredEmail;

    @Value("${app.oauth.demo-stub-enabled:true}")
    private boolean oauthDemoStubEnabled;

    @Override
    @Transactional
    public TokenResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        User user = userRepository.findByEmailIgnoreCaseAndDeletedFalse(request.getEmail())
                .orElseThrow(() -> new BusinessException("User not found"));
        requireActive(user);
        return buildTokens(user);
    }

    @Override
    @Transactional
    public RegisterPendingResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCaseAndDeletedFalse(request.getEmail())) {
            throw new BusinessException("Email already registered");
        }
        PasswordPolicy.requireValid(request.getPassword());

        User user = new User();
        user.setEmail(request.getEmail().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setRole(UserRole.CUSTOMER);
        user.setStatus(AccountStatus.PENDING_EMAIL_VERIFICATION);
        user.setJoinDate(Instant.now());
        user.setEmailVerified(false);
        user.setAvatar("https://img.rocket.new/generatedImages/rocket_gen_img_default.png");
        user.setAvatarAlt("User avatar");
        userRepository.save(user);

        sendRegistrationChallenge(user.getEmail());

        return RegisterPendingResponse.builder()
                .email(user.getEmail())
                .verificationRequired(true)
                .message(registrationInstructionMessage())
                .build();
    }

    private String registrationInstructionMessage() {
        return registrationRequireDeliveredEmail
                ? "Verification email sent. Enter the OTP with POST /api/auth/register/confirm to activate your account."
                : "Account created pending email verification (dev/local: SMTP optional; OTP is stored)."
                        + " Use POST /api/auth/register/confirm after you receive or read the OTP.";
    }

    private void sendRegistrationChallenge(String email) {
        String code = generateOtp();
        if (registrationRequireDeliveredEmail) {
            otpMailService.sendOtpMandatory(email, code, OtpPurposes.REGISTER_EMAIL);
        } else {
            otpMailService.sendOtpEmail(email, code, OtpPurposes.REGISTER_EMAIL);
        }
        saveOtp(email, OtpPurposes.REGISTER_EMAIL, code);
    }

    @Override
    @Transactional
    public TokenResponse confirmRegistration(RegisterVerifyRequest request) {
        String email = request.getEmail().toLowerCase();
        User user = userRepository.findByEmailIgnoreCaseAndDeletedFalse(email)
                .orElseThrow(() -> new BusinessException("Invalid or expired verification"));
        if (user.getStatus() != AccountStatus.PENDING_EMAIL_VERIFICATION) {
            throw new BusinessException("No pending verification for this account");
        }
        validateOtp(email, OtpPurposes.REGISTER_EMAIL, request.getCode());
        user.setStatus(AccountStatus.ACTIVE);
        user.setEmailVerified(true);
        userRepository.save(user);
        return buildTokens(user);
    }

    @Override
    @Transactional
    public TokenResponse refresh(String refreshToken) {
        RefreshToken token = refreshTokenRepository
                .findByTokenAndRevokedFalseAndDeletedFalse(refreshToken)
                .orElseThrow(() -> new BusinessException("Invalid refresh token"));
        if (token.getExpiresAt().isBefore(Instant.now())) {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            throw new BusinessException("Refresh token expired");
        }
        User user = token.getUser();
        requireActive(user);

        token.setRevoked(true);
        refreshTokenRepository.save(token);

        return buildTokens(user);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository
                .findByTokenAndRevokedFalseAndDeletedFalse(refreshToken)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }

    @Override
    @Transactional
    public void logoutAll() {
        refreshTokenRepository.revokeAllActiveByUserId(SecurityUtils.currentUserId());
    }

    @Override
    @Transactional
    public void sendOtp(OtpRequest request) {
        String purpose = normalizePurpose(request.getPurpose());
        String email = request.getEmail().toLowerCase();

        if (OtpPurposes.PASSWORD_RESET.equals(purpose)) {
            if (userRepository.findByEmailIgnoreCaseAndDeletedFalse(email)
                            .filter(u -> u.getStatus() == AccountStatus.ACTIVE)
                            .isEmpty()) {
                return;
            }
        } else if (OtpPurposes.REGISTER_EMAIL.equals(purpose)) {
            if (userRepository.findByEmailIgnoreCaseAndDeletedFalse(email)
                            .filter(u -> u.getStatus() == AccountStatus.PENDING_EMAIL_VERIFICATION)
                            .isEmpty()) {
                return;
            }
        }

        String code = generateOtp();
        otpMailService.sendOtpEmail(email, code, purpose);
        saveOtp(email, purpose, code);
    }

    @Override
    @Transactional(readOnly = true)
    public void verifyOtp(OtpVerifyRequest request) {
        String purpose = normalizePurpose(request.getPurpose());
        if (OtpPurposes.REGISTER_EMAIL.equals(purpose)) {
            throw new BusinessException("Use POST /api/auth/register/confirm to complete registration");
        }
        validateOtp(request.getEmail(), purpose, request.getCode());
    }

    @Override
    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        validateOtp(request.getEmail(), OtpPurposes.PASSWORD_RESET, request.getCode());
        PasswordPolicy.requireValid(request.getNewPassword());
        User user = userRepository.findByEmailIgnoreCaseAndDeletedFalse(request.getEmail())
                .orElseThrow(() -> new BusinessException("User not found"));
        if (user.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException("Account is not eligible for password reset");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        refreshTokenRepository.revokeAllActiveByUserId(user.getId());
    }

    @Override
    @Transactional
    public TokenResponse oauthLogin(String provider) {
        if (!oauthDemoStubEnabled) {
            throw new BusinessException("Demo OAuth stub is disabled. Use browser OAuth2 login or enable app.oauth.demo-stub-enabled.");
        }
        String email = provider + "_user@beautyskin.demo";
        User user =
                userRepository.findByEmailIgnoreCaseAndDeletedFalse(email).orElseGet(() -> {
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
        requireActive(user);
        return buildTokens(user);
    }

    private void saveOtp(String email, String purpose, String code) {
        OtpVerification otp = new OtpVerification();
        otp.setEmail(email);
        otp.setPurpose(purpose);
        otp.setCode(code);
        otp.setExpiresAt(Instant.now().plusSeconds(otpExpirationMinutes * 60L));
        otp.setUsed(false);
        otpRepository.save(otp);
    }

    private TokenResponse buildTokens(User user) {
        requireActive(user);
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

    private void requireActive(User user) {
        if (user.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException(accountStatusMessage(user.getStatus()));
        }
    }

    private static String accountStatusMessage(AccountStatus status) {
        return switch (status) {
            case PENDING_EMAIL_VERIFICATION -> "Verify your email to activate this account.";
            case INACTIVE -> "Account is inactive";
            case SUSPENDED -> "Account is suspended";
            default -> "Account is not active";
        };
    }

    private void validateOtp(String email, String purpose, String code) {
        OtpVerification otp = otpRepository
                .findTopByEmailAndPurposeAndUsedFalseAndDeletedFalseOrderByCreatedAtDesc(
                        email.toLowerCase(), purpose)
                .orElseThrow(() -> new BusinessException("OTP not found"));
        if (otp.getExpiresAt().isBefore(Instant.now())) {
            throw new BusinessException("OTP expired");
        }
        boolean demoMatch = StringUtils.hasText(demoOtpCode) && demoOtpCode.equals(code);
        if (!otp.getCode().equals(code) && !demoMatch) {
            throw new BusinessException("Invalid OTP code");
        }
        otp.setUsed(true);
        otpRepository.save(otp);
    }

    private static String normalizePurpose(String purpose) {
        if (purpose == null || purpose.isBlank()) {
            return purpose;
        }
        return purpose.trim().toUpperCase();
    }

    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(1_000_000));
    }
}
