package com.acleda.bsonlineshop.mapper;

import com.acleda.bsonlineshop.dto.inventory.InventoryItemResponse;
import com.acleda.bsonlineshop.entity.InventoryItem;
import org.springframework.stereotype.Component;

@Component
public class InventoryMapper {

    public InventoryItemResponse toResponse(InventoryItem item) {
        return InventoryItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .sku(item.getSku())
                .shopId(item.getShop() != null ? item.getShop().getId() : null)
                .currentStock(item.getCurrentStock())
                .minStock(item.getMinStock())
                .maxStock(item.getMaxStock())
                .reorderPoint(item.getReorderPoint())
                .lastRestocked(item.getLastRestocked())
                .expiryDate(item.getExpiryDate())
                .batchNumber(item.getBatchNumber())
                .supplierId(item.getSupplierId())
                .supplierName(item.getSupplierName())
                .costPrice(item.getCostPrice())
                .invStatus(item.getInvStatus())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
