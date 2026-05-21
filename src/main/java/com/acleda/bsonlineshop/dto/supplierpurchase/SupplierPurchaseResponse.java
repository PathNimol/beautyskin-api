package com.acleda.bsonlineshop.dto.supplierpurchase;

import com.acleda.bsonlineshop.enums.SupplierPurchaseStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SupplierPurchaseResponse {
    private UUID id;
    private String purchaseRef;
    private UUID supplierId;
    private String supplierName;
    private UUID shopId;
    private LocalDate orderDate;
    private LocalDate expectedDate;
    private SupplierPurchaseStatus status;
    private BigDecimal total;
    private List<SupplierPurchaseLineResponse> lines;
    private Instant createdAt;
}
