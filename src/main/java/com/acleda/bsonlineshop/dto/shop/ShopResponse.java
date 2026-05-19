package com.acleda.bsonlineshop.dto.shop;

import com.acleda.bsonlineshop.enums.ShopPlan;
import com.acleda.bsonlineshop.enums.ShopStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShopResponse {
    private UUID id;
    private String name;
    private String slug;
    private UUID ownerId;
    private String ownerName;
    private String logo;
    private String logoAlt;
    private String description;
    private ShopStatus status;
    private ShopPlan plan;
    private BigDecimal revenue;
    private int ordersCount;
    private int productsCount;
    private int customersCount;
    private String category;
    private Instant createdAt;
}
