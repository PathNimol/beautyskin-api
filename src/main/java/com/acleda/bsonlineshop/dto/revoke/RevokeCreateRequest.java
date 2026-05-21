package com.acleda.bsonlineshop.dto.revoke;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Data;

@Data
public class RevokeCreateRequest {
    @NotNull
    private UUID productId;
    @NotNull
    @Min(1)
    private Integer quantity;
    @NotBlank
    private String reason;
    private String detail;
}
