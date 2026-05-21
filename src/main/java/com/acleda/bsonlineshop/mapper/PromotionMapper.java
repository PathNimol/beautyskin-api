package com.acleda.bsonlineshop.mapper;

import com.acleda.bsonlineshop.dto.promotion.PromotionCreateRequest;
import com.acleda.bsonlineshop.dto.promotion.PromotionResponse;
import com.acleda.bsonlineshop.dto.promotion.PromotionUpdateRequest;
import com.acleda.bsonlineshop.entity.Promotion;
import com.acleda.bsonlineshop.entity.Shop;
import com.acleda.bsonlineshop.enums.PromotionStatus;
import org.springframework.stereotype.Component;

@Component
public class PromotionMapper {

    public PromotionResponse toResponse(Promotion p) {
        return PromotionResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .code(p.getCode())
                .type(p.getType())
                .value(p.getValue())
                .minOrder(p.getMinOrder())
                .maxUses(p.getMaxUses())
                .usedCount(p.getUsedCount())
                .startDate(p.getStartDate())
                .endDate(p.getEndDate())
                .status(p.getStatus())
                .shopId(p.getShop() != null ? p.getShop().getId() : null)
                .description(p.getDescription())
                .createdAt(p.getCreatedAt())
                .build();
    }

    public void applyCreate(Promotion p, Shop shop, PromotionCreateRequest req) {
        p.setShop(shop);
        p.setName(req.getName());
        p.setCode(req.getCode().toUpperCase());
        p.setType(req.getType());
        p.setValue(req.getValue());
        p.setMinOrder(req.getMinOrder() != null ? req.getMinOrder() : java.math.BigDecimal.ZERO);
        p.setMaxUses(req.getMaxUses() != null ? req.getMaxUses() : 100);
        p.setUsedCount(0);
        p.setStartDate(req.getStartDate());
        p.setEndDate(req.getEndDate());
        p.setStatus(PromotionStatus.ACTIVE);
        p.setDescription(req.getDescription() != null ? req.getDescription() : "");
    }

    public void applyUpdate(Promotion p, PromotionUpdateRequest req) {
        if (req.getName() != null) p.setName(req.getName());
        if (req.getCode() != null) p.setCode(req.getCode().toUpperCase());
        if (req.getType() != null) p.setType(req.getType());
        if (req.getValue() != null) p.setValue(req.getValue());
        if (req.getMinOrder() != null) p.setMinOrder(req.getMinOrder());
        if (req.getMaxUses() != null) p.setMaxUses(req.getMaxUses());
        if (req.getStartDate() != null) p.setStartDate(req.getStartDate());
        if (req.getEndDate() != null) p.setEndDate(req.getEndDate());
        if (req.getDescription() != null) p.setDescription(req.getDescription());
    }
}
