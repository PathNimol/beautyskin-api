package com.acleda.bsonlineshop.mapper;

import com.acleda.bsonlineshop.dto.shop.ShopNameChangeResponse;
import com.acleda.bsonlineshop.entity.ShopNameChangeRequest;
import org.springframework.stereotype.Component;

@Component
public class ShopNameChangeMapper {

    public ShopNameChangeResponse toResponse(ShopNameChangeRequest r) {
        return ShopNameChangeResponse.builder()
                .id(r.getId())
                .shopId(r.getShop() != null ? r.getShop().getId() : null)
                .shopName(r.getShop() != null ? r.getShop().getName() : null)
                .ownerName(r.getShop() != null ? r.getShop().getOwnerName() : null)
                .currentName(r.getCurrentName())
                .requestedName(r.getRequestedName())
                .status(r.getStatus())
                .reviewNotes(r.getReviewNotes())
                .reviewedBy(r.getReviewedBy())
                .requestedBy(r.getRequestedBy())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
