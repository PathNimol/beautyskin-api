package com.acleda.bsonlineshop.mapper;

import com.acleda.bsonlineshop.dto.revoke.RevokeRequestResponse;
import com.acleda.bsonlineshop.entity.ProductRevokeRequest;
import org.springframework.stereotype.Component;

@Component
public class RevokeRequestMapper {

    public RevokeRequestResponse toResponse(ProductRevokeRequest r) {
        return RevokeRequestResponse.builder()
                .id(r.getId())
                .shopId(r.getShop() != null ? r.getShop().getId() : null)
                .productId(r.getProduct() != null ? r.getProduct().getId() : null)
                .productName(r.getProduct() != null ? r.getProduct().getName() : null)
                .sku(r.getSku())
                .quantity(r.getQuantity())
                .reason(r.getReason())
                .detail(r.getDetail())
                .status(r.getStatus())
                .reviewNotes(r.getReviewNotes())
                .reviewedBy(r.getReviewedBy())
                .requesterEmail(r.getRequesterEmail())
                .requesterName(r.getRequesterName())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
