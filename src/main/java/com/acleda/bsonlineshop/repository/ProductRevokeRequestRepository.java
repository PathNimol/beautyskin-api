package com.acleda.bsonlineshop.repository;

import com.acleda.bsonlineshop.entity.ProductRevokeRequest;
import com.acleda.bsonlineshop.enums.RevokeRequestStatus;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRevokeRequestRepository extends JpaRepository<ProductRevokeRequest, UUID> {

    @Query("""
            SELECT r FROM ProductRevokeRequest r WHERE r.deleted = false
            AND r.shop.id = :shopId
            AND (:status IS NULL OR r.status = :status)
            """)
    Page<ProductRevokeRequest> findByShop(
            @Param("shopId") UUID shopId, @Param("status") RevokeRequestStatus status, Pageable pageable);
}
