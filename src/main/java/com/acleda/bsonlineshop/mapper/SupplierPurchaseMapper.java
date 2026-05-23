package com.acleda.bsonlineshop.mapper;

import com.acleda.bsonlineshop.dto.supplierpurchase.SupplierPurchaseLineResponse;
import com.acleda.bsonlineshop.dto.supplierpurchase.SupplierPurchaseResponse;
import com.acleda.bsonlineshop.entity.SupplierPurchase;
import com.acleda.bsonlineshop.entity.SupplierPurchaseLine;
import org.springframework.stereotype.Component;

@Component
public class SupplierPurchaseMapper {

    public SupplierPurchaseResponse toResponse(SupplierPurchase p) {
        return SupplierPurchaseResponse.builder()
                .id(p.getId())
                .purchaseRef(p.getPurchaseRef())
                .supplierId(p.getSupplier() != null ? p.getSupplier().getId() : null)
                .supplierName(p.getSupplier() != null ? p.getSupplier().getName() : null)
                .shopId(p.getShop() != null ? p.getShop().getId() : null)
                .orderDate(p.getOrderDate())
                .expectedDate(p.getExpectedDate())
                .status(p.getStatus())
                .total(p.getTotal())
                .lines(p.getLines().stream().map(this::toLine).toList())
                .createdAt(p.getCreatedAt())
                .build();
    }

    private SupplierPurchaseLineResponse toLine(SupplierPurchaseLine line) {
        return SupplierPurchaseLineResponse.builder()
                .productId(line.getProductId())
                .productName(line.getProductName())
                .sku(line.getSku())
                .quantity(line.getQuantity())
                .unitCost(line.getUnitCost())
                .lineTotal(line.getLineTotal())
                .build();
    }
}
