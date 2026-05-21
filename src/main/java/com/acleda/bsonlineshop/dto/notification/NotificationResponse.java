package com.acleda.bsonlineshop.dto.notification;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationResponse {
    private UUID id;
    private UUID shopId;
    private String type;
    private String title;
    private String message;
    private boolean read;
    private String link;
    private Instant createdAt;
}
