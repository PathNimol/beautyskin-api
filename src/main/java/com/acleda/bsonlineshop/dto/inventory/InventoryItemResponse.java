package com.acleda.bsonlineshop.dto.inventory;

import com.acleda.bsonlineshop.enums.InventoryStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InventoryItemResponse {
    private UUID id;
    private UUID productId;
    private String productName;
    private String sku;
    private UUID shopId;
    private int currentStock;
    private int minStock;
    private int maxStock;
    private int reorderPoint;
    private LocalDate lastRestocked;
    private LocalDate expiryDate;
    private String batchNumber;
    private UUID supplierId;
    private String supplierName;
    private BigDecimal costPrice;
    private InventoryStatus invStatus;
    private Instant createdAt;
    private Instant updatedAt;
}
