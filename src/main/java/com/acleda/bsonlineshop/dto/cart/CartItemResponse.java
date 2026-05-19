package com.acleda.bsonlineshop.dto.cart;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartItemResponse {
    private UUID id;
    private UUID productId;
    private String name;
    private String brand;
    private BigDecimal price;
    private int quantity;
    private String image;
    private String imageAlt;
    private UUID shopId;
    private String shopName;
    private BigDecimal lineTotal;
}
