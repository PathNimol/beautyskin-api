package com.acleda.bsonlineshop.dto.review;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewResponse {
    private UUID id;
    private UUID productId;
    private UUID customerId;
    private String customerName;
    private String customerAvatar;
    private String customerAvatarAlt;
    private int rating;
    private String title;
    private String body;
    private List<String> photos;
    private boolean verified;
    private int helpful;
    private String skinType;
    private Instant createdAt;
}
