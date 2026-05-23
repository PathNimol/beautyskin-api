package com.acleda.bsonlineshop.dto.shop;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ShopNameChangeCreateRequest {
    @NotBlank
    @Size(min = 2, max = 120)
    private String requestedName;
}
