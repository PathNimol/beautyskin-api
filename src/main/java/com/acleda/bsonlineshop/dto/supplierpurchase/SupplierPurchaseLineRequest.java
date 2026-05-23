package com.acleda.bsonlineshop.dto.supplierpurchase;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Data;

@Data
public class SupplierPurchaseLineRequest {
    private UUID productId;
    @NotBlank
    private String productName;
    private String sku;
    @NotNull
    @Min(1)
    private Integer quantity;
    @NotNull
    private BigDecimal unitCost;
}
