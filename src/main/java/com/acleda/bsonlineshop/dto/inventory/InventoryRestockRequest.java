package com.acleda.bsonlineshop.dto.inventory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InventoryRestockRequest {
    @NotNull
    @Min(1)
    private Integer quantity;
}
