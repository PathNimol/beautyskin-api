package com.beautyskin.api.dto.response;

import lombok.Builder;

@Builder
public record TokenResponse(
    String accessToken, String refreshToken, long expiresIn, UserResponse user) {}
