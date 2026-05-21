package com.acleda.bsonlineshop.dto.revoke;

import com.acleda.bsonlineshop.enums.RevokeRequestStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RevokeReviewRequest {
    @NotNull
    private RevokeRequestStatus status;
    private String notes;
}
