package com.acleda.bsonlineshop.entity;

import com.acleda.bsonlineshop.enums.InventoryStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "inventory_items")
public class InventoryItem extends BaseEntity {

    private UUID productId;

    @Column(nullable = false)
    private String productName;

    private String sku;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    private int currentStock;
    private int minStock;
    private int maxStock;
    private int reorderPoint;
    private LocalDate lastRestocked;
    private LocalDate expiryDate;
    private String batchNumber;

    private UUID supplierId;
    private String supplierName;

    @Column(precision = 12, scale = 2)
    private BigDecimal costPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InventoryStatus invStatus = InventoryStatus.HEALTHY;
}
