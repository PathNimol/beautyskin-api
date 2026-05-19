package com.acleda.bsonlineshop.repository;

import com.acleda.bsonlineshop.entity.SupplierPurchase;
import com.acleda.bsonlineshop.enums.SupplierPurchaseStatus;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SupplierPurchaseRepository extends JpaRepository<SupplierPurchase, UUID> {

    @Query("""
            SELECT p FROM SupplierPurchase p WHERE p.deleted = false
            AND (:shopId IS NULL OR p.shop.id = :shopId)
            AND (:status IS NULL OR p.status = :status)
            """)
    Page<SupplierPurchase> search(
            @Param("shopId") UUID shopId,
            @Param("status") SupplierPurchaseStatus status,
            Pageable pageable);
}
