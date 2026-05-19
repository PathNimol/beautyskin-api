package com.acleda.bsonlineshop.dto.product;

import com.acleda.bsonlineshop.enums.ProductStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductResponse {
    private UUID id;
    private String name;
    private String brand;
    private String category;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private int stock;
    private int sold;
    private double rating;
    private int reviewCount;
    private String image;
    private String imageAlt;
    private List<ImageDto> images;
    private String description;
    private List<String> ingredients;
    private String howToUse;
    private List<String> skinTypes;
    private LocalDate expiryDate;
    private String sku;
    private UUID shopId;
    private String shopName;
    private ProductStatus status;
    private List<String> tags;
    private String weight;
    private String origin;
    private boolean visible;
}
