package com.acleda.bsonlineshop.entity;

import com.acleda.bsonlineshop.enums.ProductStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "products")
public class Product extends BaseEntity {

    @Column(nullable = false)
    private String name;

    private String brand;
    private String category;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(precision = 12, scale = 2)
    private BigDecimal originalPrice;

    private int stock;
    private int sold;
    private double rating;
    private int reviewCount;

    private String image;
    private String imageAlt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    private List<ProductImage> images = new ArrayList<>();

    @Column(length = 4000)
    private String description;

    @ElementCollection
    @CollectionTable(name = "product_ingredients", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "ingredient")
    private List<String> ingredients = new ArrayList<>();

    @Column(length = 2000)
    private String howToUse;

    @ElementCollection
    @CollectionTable(name = "product_skin_types", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "skin_type")
    private List<String> skinTypes = new ArrayList<>();

    private LocalDate expiryDate;
    private String sku;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.ACTIVE;

    @ElementCollection
    @CollectionTable(name = "product_tags", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    private String weight;
    private String origin;

    private boolean visible = true;
    private boolean revoked;
}
