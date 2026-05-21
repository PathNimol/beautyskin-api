package com.acleda.bsonlineshop.dto.supplierpurchase;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class SupplierPurchaseCreateRequest {
    @NotNull
    private UUID supplierId;
    private LocalDate expectedDate;
    @NotEmpty
    @Valid
    private List<SupplierPurchaseLineRequest> items;
}
