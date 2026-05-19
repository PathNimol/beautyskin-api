package com.acleda.bsonlineshop.dto.order;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderLineResponse {
    private UUID productId;
    private String name;
    private String brand;
    private int qty;
    private BigDecimal price;
    private String image;
    private String imageAlt;
}
