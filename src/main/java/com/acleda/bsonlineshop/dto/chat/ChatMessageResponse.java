package com.acleda.bsonlineshop.dto.chat;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatMessageResponse {
    private UUID id;
    private UUID roomId;
    private UUID senderId;
    private String senderName;
    private String senderRole;
    private String content;
    private Instant createdAt;
}
