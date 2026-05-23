package com.acleda.bsonlineshop.dto.auth;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RegisterPendingResponse {
    String email;
    boolean verificationRequired;
    String message;
}
