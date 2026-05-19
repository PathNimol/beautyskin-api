package com.acleda.bsonlineshop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "suppliers")
public class Supplier extends BaseEntity {

    @Column(nullable = false)
    private String name;

    private String contactPerson;
    private String email;
    private String phone;
    private String address;
    private String country;
    private String category;

    private int totalOrders;

    @Column(precision = 14, scale = 2)
    private BigDecimal totalSpent;

    private double rating;
    private String status;
    private LocalDate joinDate;
    private LocalDate lastOrder;
    private String logo;
    private String logoAlt;
}
