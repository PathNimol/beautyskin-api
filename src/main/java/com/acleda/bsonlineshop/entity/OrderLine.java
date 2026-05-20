package com.acleda.bsonlineshop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "order_lines")
public class OrderLine extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    private UUID productId;

    @Column(nullable = false)
    private String name;

    private int qty;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    private String image;
    private String imageAlt;
    private String brand;
}
