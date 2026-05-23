package com.acleda.bsonlineshop.dto.shop;

import com.acleda.bsonlineshop.enums.RevokeRequestStatus;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShopNameChangeResponse {
    private UUID id;
    private UUID shopId;
    private String shopName;
    private String ownerName;
    private String currentName;
    private String requestedName;
    private RevokeRequestStatus status;
    private String reviewNotes;
    private String reviewedBy;
    private UUID requestedBy;
    private Instant createdAt;
}
