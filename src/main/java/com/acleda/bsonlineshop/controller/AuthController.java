package com.acleda.bsonlineshop.controller;

import com.acleda.bsonlineshop.auth.OtpPurposes;
import com.acleda.bsonlineshop.dto.auth.LoginRequest;
import com.acleda.bsonlineshop.dto.auth.LogoutRequest;
import com.acleda.bsonlineshop.dto.auth.OtpRequest;
import com.acleda.bsonlineshop.dto.auth.OtpVerifyRequest;
import com.acleda.bsonlineshop.dto.auth.PasswordResetRequest;
import com.acleda.bsonlineshop.dto.auth.RegisterPendingResponse;
import com.acleda.bsonlineshop.dto.auth.RegisterRequest;
import com.acleda.bsonlineshop.dto.auth.RegisterVerifyRequest;
import com.acleda.bsonlineshop.dto.auth.TokenResponse;
import com.acleda.bsonlineshop.dto.common.ApiResponse;
import com.acleda.bsonlineshop.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Login", description = "Authenticate with email and password. Returns access and refresh tokens. Only ACTIVE accounts can sign in.")
    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success("Login successful", authService.login(request));
    }

    @Operation(
            summary = "Register customer",
            description =
                    "Create a CUSTOMER account in PENDING_EMAIL_VERIFICATION (no tokens). Sends or stores OTP for purpose REGISTER_EMAIL. "
                            + "Complete with POST /auth/register/confirm. When app.auth.registration.require-delivered-email=true (recommended in prod), "
                            + "SMTP must be configured or the registration fails rolled back.")
    @PostMapping("/register")
    public ApiResponse<RegisterPendingResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success("Verification required", authService.register(request));
    }

    @Operation(
            summary = "Confirm registration",
            description = "Validate REGISTER_EMAIL OTP, mark account ACTIVE + emailVerified, return tokens. Do not use /otp/verify for REGISTER_EMAIL.")
    @PostMapping("/register/confirm")
    public ApiResponse<TokenResponse> confirmRegistration(@Valid @RequestBody RegisterVerifyRequest request) {
        return ApiResponse.success("Account activated", authService.confirmRegistration(request));
    }

    @Operation(summary = "Refresh tokens", description = "Exchange a valid refreshToken for new access and refresh tokens. The previous refresh token is revoked (rotation).")
    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(@RequestBody Map<String, String> body) {
        return ApiResponse.success(authService.refresh(body.get("refreshToken")));
    }

    @Operation(summary = "Logout", description = "Revoke the given refresh token for this device/session.")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request.getRefreshToken());
        return ApiResponse.success("Logged out", null);
    }

    @Operation(summary = "Logout all devices", description = "Revoke all active refresh tokens for the authenticated user. Requires Bearer access token.")
    @PostMapping("/logout-all")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<Void> logoutAll() {
        authService.logoutAll();
        return ApiResponse.success("Logged out from all devices", null);
    }

    @Operation(
            summary = "Send OTP",
            description =
                    "Generate and store OTP. PASSWORD_RESET only for ACTIVE accounts (silent noop otherwise). "
                            + "REGISTER_EMAIL only for pending registrations. Dev: optional demo-code when app.otp.demo-code is non-empty.")
    @PostMapping("/otp/send")
    public ApiResponse<Void> sendOtp(@Valid @RequestBody OtpRequest request) {
        authService.sendOtp(request);
        return ApiResponse.success("OTP sent", null);
    }

    @Operation(
            summary = "Verify OTP",
            description = "Validate OTP without side effects other than marking it used. Not for REGISTER_EMAIL — use /auth/register/confirm.")
    @PostMapping("/otp/verify")
    public ApiResponse<Void> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        authService.verifyOtp(request);
        return ApiResponse.success("OTP verified", null);
    }

    @Operation(summary = "Forgot password", description = "Send OTP for PASSWORD_RESET when an ACTIVE account exists (same response either way).")
    @PostMapping("/password/forgot")
    public ApiResponse<Void> forgotPassword(@Valid @RequestBody OtpRequest request) {
        request.setPurpose(OtpPurposes.PASSWORD_RESET);
        authService.sendOtp(request);
        return ApiResponse.success("If an account exists, a reset code was sent", null);
    }

    @Operation(summary = "Reset password", description = "After OTP verification, set a new password (policy applies). Revokes all refresh tokens for that user.")
    @PostMapping("/password/reset")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        authService.resetPassword(request);
        return ApiResponse.success("Password updated", null);
    }

    @Operation(
            summary = "Demo OAuth login",
            description =
                    "Development stub: issues tokens for a synthetic user. Disabled when app.oauth.demo-stub-enabled=false (e.g. prod). Real Google sign-in uses the OAuth2 authorization endpoint.")
    @PostMapping("/oauth/{provider}")
    public ApiResponse<TokenResponse> oauth(@PathVariable String provider) {
        return ApiResponse.success(authService.oauthLogin(provider));
    }
}
