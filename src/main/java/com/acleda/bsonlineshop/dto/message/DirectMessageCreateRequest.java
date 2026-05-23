package com.acleda.bsonlineshop.dto.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Data;

@Data
public class DirectMessageCreateRequest {
    @NotNull
    private UUID recipientId;
    @NotBlank
    private String content;
}
