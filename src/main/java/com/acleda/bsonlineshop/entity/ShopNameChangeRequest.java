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
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "shop_name_change_requests")
public class ShopNameChangeRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @Column(nullable = false)
    private String currentName;

    @Column(nullable = false)
    private String requestedName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RevokeRequestStatus status = RevokeRequestStatus.PENDING;

    @Column(length = 2000)
    private String reviewNotes;

    private String reviewedBy;

    private UUID requestedBy;
}
