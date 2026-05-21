package com.acleda.bsonlineshop.dto.supplierpurchase;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SupplierPurchaseLineResponse {
    private UUID productId;
    private String productName;
    private String sku;
    private int quantity;
    private BigDecimal unitCost;
    private BigDecimal lineTotal;
}
