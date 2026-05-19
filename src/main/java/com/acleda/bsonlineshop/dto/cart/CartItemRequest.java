package com.acleda.bsonlineshop.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Data;

@Data
public class CartItemRequest {
    @NotNull
    private UUID productId;
    @Min(1)
    private int quantity = 1;
}
