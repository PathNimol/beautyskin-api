package com.acleda.bsonlineshop.dto.message;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DirectMessageResponse {
    private UUID id;
    private UUID threadId;
    private UUID senderId;
    private UUID recipientId;
    private String content;
    private boolean read;
    private Instant createdAt;
}
