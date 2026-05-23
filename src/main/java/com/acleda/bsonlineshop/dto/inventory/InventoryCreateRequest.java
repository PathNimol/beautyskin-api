package com.acleda.bsonlineshop.dto.inventory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Data;

@Data
public class InventoryCreateRequest {
    @NotNull
    private UUID productId;
    @NotBlank
    private String productName;
    private String sku;
    @NotNull
    private Integer currentStock;
    private Integer minStock;
    private Integer maxStock;
    private Integer reorderPoint;
    private LocalDate expiryDate;
    private String batchNumber;
    private UUID supplierId;
    private String supplierName;
    private BigDecimal costPrice;
}
