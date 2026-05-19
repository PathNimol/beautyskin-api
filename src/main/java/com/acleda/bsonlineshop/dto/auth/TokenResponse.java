package com.acleda.bsonlineshop.dto.auth;

import com.acleda.bsonlineshop.dto.user.UserResponse;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenResponse {
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private UserResponse user;
}
