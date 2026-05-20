package com.acleda.bsonlineshop.entity;

import com.acleda.bsonlineshop.enums.ShopPlan;
import com.acleda.bsonlineshop.enums.ShopStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "shops")
public class Shop extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    private UUID ownerId;
    private String ownerName;
    private String logo;
    private String logoAlt;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShopStatus status = ShopStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShopPlan plan = ShopPlan.STARTER;

    private BigDecimal revenue = BigDecimal.ZERO;
    private int ordersCount;
    private int productsCount;
    private int customersCount;
    private String category;
}
