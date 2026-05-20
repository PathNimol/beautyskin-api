package com.acleda.bsonlineshop.entity;

import com.acleda.bsonlineshop.enums.RevokeRequestStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "product_revoke_requests")
public class ProductRevokeRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private String sku;
    private int quantity;
    private String reason;

    @Column(length = 2000)
    private String detail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RevokeRequestStatus status = RevokeRequestStatus.PENDING;

    private String reviewNotes;
    private String reviewedBy;
}
