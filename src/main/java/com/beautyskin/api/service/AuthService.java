package com.beautyskin.api.service;

import com.beautyskin.api.dto.request.LoginRequest;
import com.beautyskin.api.dto.request.RefreshTokenRequest;
import com.beautyskin.api.dto.request.RegisterRequest;
import com.beautyskin.api.dto.response.TokenResponse;
import com.beautyskin.api.dto.response.UserResponse;
import com.beautyskin.api.model.entity.User;

public interface AuthService {

  TokenResponse login(LoginRequest request);

  TokenResponse register(RegisterRequest request);

  TokenResponse refresh(RefreshTokenRequest request);

  UserResponse getCurrentUser(User user);
}
