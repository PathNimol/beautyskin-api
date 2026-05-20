package com.acleda.bsonlineshop.dto.shop;

import com.acleda.bsonlineshop.enums.ShopPlan;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ShopCreateRequest {
    @NotBlank
    private String name;
    private String description;
    private String category;
    private String logo;
    private String logoAlt;
    private ShopPlan plan;
    private String ownerName;
}
