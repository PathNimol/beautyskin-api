package com.beautyskin.api.controller;

import com.beautyskin.api.dto.request.LoginRequest;
import com.beautyskin.api.dto.request.RefreshTokenRequest;
import com.beautyskin.api.dto.request.RegisterRequest;
import com.beautyskin.api.dto.response.TokenResponse;
import com.beautyskin.api.service.AuthService;
import com.beautyskin.api.utils.ApiResponse;
import com.beautyskin.api.utils.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Login, register, and token refresh")
public class AuthController extends BaseResponse {

  private final AuthService authService;

  @PostMapping("/login")
  @Operation(summary = "Login with email and password")
  public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
    return ok("Login successful", authService.login(request));
  }

  @PostMapping("/register")
  @Operation(
      summary = "Register a new customer account",
      description =
          "Creates a customer account. Role is assigned by the server. "
              + "Password and confirmPassword must match.")
  public ResponseEntity<ApiResponse<TokenResponse>> register(
      @Valid @RequestBody RegisterRequest request) {
    return created("Registration successful", authService.register(request));
  }

  @PostMapping("/refresh")
  @Operation(summary = "Refresh access token")
  public ResponseEntity<ApiResponse<TokenResponse>> refresh(
      @Valid @RequestBody RefreshTokenRequest request) {
    return ok("Token refreshed", authService.refresh(request));
  }
}
