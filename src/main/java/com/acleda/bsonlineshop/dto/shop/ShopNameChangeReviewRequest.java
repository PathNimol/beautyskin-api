package com.acleda.bsonlineshop.dto.shop;

import com.acleda.bsonlineshop.enums.RevokeRequestStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ShopNameChangeReviewRequest {
    @NotNull
    private RevokeRequestStatus status;

    private String notes;
}
