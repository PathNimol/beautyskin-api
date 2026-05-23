package com.acleda.bsonlineshop.service;

import com.acleda.bsonlineshop.dto.auth.LoginRequest;
import com.acleda.bsonlineshop.dto.auth.OtpRequest;
import com.acleda.bsonlineshop.dto.auth.OtpVerifyRequest;
import com.acleda.bsonlineshop.dto.auth.PasswordResetRequest;
import com.acleda.bsonlineshop.dto.auth.RegisterPendingResponse;
import com.acleda.bsonlineshop.dto.auth.RegisterRequest;
import com.acleda.bsonlineshop.dto.auth.RegisterVerifyRequest;
import com.acleda.bsonlineshop.dto.auth.TokenResponse;

public interface AuthService {
    TokenResponse login(LoginRequest request);

    RegisterPendingResponse register(RegisterRequest request);

    TokenResponse confirmRegistration(RegisterVerifyRequest request);

    TokenResponse refresh(String refreshToken);

    void logout(String refreshToken);

    void logoutAll();

    void sendOtp(OtpRequest request);

    void verifyOtp(OtpVerifyRequest request);

    void resetPassword(PasswordResetRequest request);

    TokenResponse oauthLogin(String provider);
}
