package com.acleda.bsonlineshop.dto.chat;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatMessageCreateRequest {
    @NotBlank
    private String content;
}
