package com.acleda.bsonlineshop.dto.revoke;

import com.acleda.bsonlineshop.enums.RevokeRequestStatus;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RevokeRequestResponse {
    private UUID id;
    private UUID shopId;
    private UUID productId;
    private String productName;
    private String sku;
    private int quantity;
    private String reason;
    private String detail;
    private RevokeRequestStatus status;
    private String reviewNotes;
    private String reviewedBy;
    private String requesterEmail;
    private String requesterName;
    private Instant createdAt;
}
