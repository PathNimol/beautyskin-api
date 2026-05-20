package com.acleda.bsonlineshop.dto.product;

import com.acleda.bsonlineshop.enums.ProductStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class ProductCreateRequest {
    @NotBlank
    private String name;
    private String brand;
    private String category;
    @NotNull
    private BigDecimal price;
    private BigDecimal originalPrice;
    private int stock;
    private String image;
    private String imageAlt;
    private List<ImageDto> images;
    private String description;
    private List<String> ingredients;
    private String howToUse;
    private List<String> skinTypes;
    private LocalDate expiryDate;
    private String sku;
    private ProductStatus status;
    private List<String> tags;
    private String weight;
    private String origin;
    private boolean visible = true;
}
