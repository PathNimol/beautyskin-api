package com.beautyskin.api.service.impl;

import com.beautyskin.api.dto.request.LoginRequest;
import com.beautyskin.api.dto.request.RefreshTokenRequest;
import com.beautyskin.api.dto.request.RegisterRequest;
import com.beautyskin.api.dto.response.TokenResponse;
import com.beautyskin.api.dto.response.UserResponse;
import com.beautyskin.api.exception.BadRequestException;
import com.beautyskin.api.exception.DuplicateResourceException;
import com.beautyskin.api.exception.UnauthorizedException;
import com.beautyskin.api.model.entity.User;
import com.beautyskin.api.model.enums.UserRole;
import com.beautyskin.api.repository.UserRepository;
import com.beautyskin.api.security.jwt.InvalidJwtException;
import com.beautyskin.api.security.jwt.JwtTokenProvider;
import com.beautyskin.api.service.AuthService;
import io.jsonwebtoken.Claims;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;

  @Override
  @Transactional(readOnly = true)
  public TokenResponse login(LoginRequest request) {
    User user =
        userRepository
            .findByEmailIgnoreCase(request.email())
            .filter(u -> Boolean.TRUE.equals(u.getEnabled()))
            .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

    if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
      throw new UnauthorizedException("Invalid email or password");
    }

    return buildTokenResponse(user);
  }

  @Override
  @Transactional
  public TokenResponse register(RegisterRequest request) {
    if (!request.password().equals(request.confirmPassword())) {
      throw new BadRequestException("Passwords do not match");
    }

    if (userRepository.existsByEmailIgnoreCase(request.email())) {
      throw new DuplicateResourceException("Email is already registered");
    }

    String avatarUrl = normalizeAvatarUrl(request.avatarUrl());

    User user =
        User.builder()
            .email(request.email().trim().toLowerCase())
            .passwordHash(passwordEncoder.encode(request.password()))
            .firstName(request.firstName().trim())
            .lastName(request.lastName().trim())
            .role(UserRole.CUSTOMER)
            .phone(blankToNull(request.phone()))
            .avatarUrl(avatarUrl)
            .enabled(true)
            .build();

    userRepository.save(user);
    return buildTokenResponse(user);
  }

  @Override
  @Transactional(readOnly = true)
  public TokenResponse refresh(RefreshTokenRequest request) {
    try {
      Claims claims = jwtTokenProvider.parseClaims(request.refreshToken());
      if (!jwtTokenProvider.isRefreshToken(claims)) {
        throw new UnauthorizedException("Invalid refresh token");
      }

      UUID userId = jwtTokenProvider.getUserId(claims);
      User user =
          userRepository
              .findById(userId)
              .filter(u -> Boolean.TRUE.equals(u.getEnabled()))
              .orElseThrow(() -> new UnauthorizedException("User not found"));

      return buildTokenResponse(user);
    } catch (InvalidJwtException e) {
      throw new UnauthorizedException("Invalid or expired refresh token");
    }
  }

  @Override
  @Transactional(readOnly = true)
  public UserResponse getCurrentUser(User user) {
    return UserResponse.from(user);
  }

  private TokenResponse buildTokenResponse(User user) {
    return TokenResponse.builder()
        .accessToken(jwtTokenProvider.createAccessToken(user))
        .refreshToken(jwtTokenProvider.createRefreshToken(user))
        .expiresIn(jwtTokenProvider.getAccessExpirationSeconds())
        .user(UserResponse.from(user))
        .build();
  }

  private static String blankToNull(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }

  private static String normalizeAvatarUrl(String avatarUrl) {
    String trimmed = blankToNull(avatarUrl);
    if (trimmed == null) {
      return null;
    }
    if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
      throw new BadRequestException("Profile picture must be a valid http(s) URL");
    }
    return trimmed;
  }
}
