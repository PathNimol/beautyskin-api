package com.acleda.bsonlineshop.dto.inventory;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InventoryAdjustRequest {
    @NotNull
    private Integer delta;
    private String notes;
}
