package com.acleda.bsonlineshop.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OtpVerifyRequest {
    @NotBlank @Email
    private String email;
    @NotBlank
    private String purpose;
    @NotBlank @Size(min = 6, max = 6)
    private String code;
}
