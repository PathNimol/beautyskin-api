package com.acleda.bsonlineshop.entity;

import com.acleda.bsonlineshop.enums.SupplierPurchaseStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
@Table(name = "supplier_purchases")
public class SupplierPurchase extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String purchaseRef;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    private Shop shop;

    private LocalDate orderDate;
    private LocalDate expectedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SupplierPurchaseStatus status = SupplierPurchaseStatus.PENDING;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal total;

    @OneToMany(mappedBy = "purchase", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SupplierPurchaseLine> lines = new ArrayList<>();
}
