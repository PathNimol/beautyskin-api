package com.acleda.bsonlineshop.controller;

import com.acleda.bsonlineshop.dto.auth.LoginRequest;
import com.acleda.bsonlineshop.dto.auth.OtpRequest;
import com.acleda.bsonlineshop.dto.auth.OtpVerifyRequest;
import com.acleda.bsonlineshop.dto.auth.PasswordResetRequest;
import com.acleda.bsonlineshop.dto.auth.RegisterRequest;
import com.acleda.bsonlineshop.dto.auth.TokenResponse;
import com.acleda.bsonlineshop.dto.common.ApiResponse;
import com.acleda.bsonlineshop.service.AuthService;
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

    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success("Login successful", authService.login(request));
    }

    @PostMapping("/register")
    public ApiResponse<TokenResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success("Registration successful", authService.register(request));
    }

    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(@RequestBody Map<String, String> body) {
        return ApiResponse.success(authService.refresh(body.get("refreshToken")));
    }

    @PostMapping("/otp/send")
    public ApiResponse<Void> sendOtp(@Valid @RequestBody OtpRequest request) {
        authService.sendOtp(request);
        return ApiResponse.success("OTP sent", null);
    }

    @PostMapping("/otp/verify")
    public ApiResponse<Void> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        authService.verifyOtp(request);
        return ApiResponse.success("OTP verified", null);
    }

    @PostMapping("/password/forgot")
    public ApiResponse<Void> forgotPassword(@Valid @RequestBody OtpRequest request) {
        request.setPurpose("PASSWORD_RESET");
        authService.sendOtp(request);
        return ApiResponse.success("Reset code sent", null);
    }

    @PostMapping("/password/reset")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        authService.resetPassword(request);
        return ApiResponse.success("Password updated", null);
    }

    @PostMapping("/oauth/{provider}")
    public ApiResponse<TokenResponse> oauth(@PathVariable String provider) {
        return ApiResponse.success(authService.oauthLogin(provider));
    }
}
