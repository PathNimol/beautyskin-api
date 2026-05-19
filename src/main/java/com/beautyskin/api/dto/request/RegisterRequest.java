package com.beautyskin.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Customer self-registration (role is assigned automatically)")
public record RegisterRequest(
    @NotBlank @Schema(example = "Emma") String firstName,
    @NotBlank @Schema(example = "Rodriguez") String lastName,
    @NotBlank @Email @Schema(example = "emma@example.com") String email,
    @Schema(example = "+855 12 345 678") String phone,
    @NotBlank @Size(min = 6, max = 100) @Schema(example = "secret12") String password,
    @NotBlank @Size(min = 6, max = 100) @Schema(example = "secret12") String confirmPassword,
    @Size(max = 2048)
        @Schema(
            description = "Profile picture URL",
            example = "https://example.com/avatars/me.jpg")
        String avatarUrl) {}
