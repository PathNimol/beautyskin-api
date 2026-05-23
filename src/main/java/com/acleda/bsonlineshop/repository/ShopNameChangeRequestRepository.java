package com.acleda.bsonlineshop.repository;

import com.acleda.bsonlineshop.entity.ShopNameChangeRequest;
import com.acleda.bsonlineshop.enums.RevokeRequestStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShopNameChangeRequestRepository extends JpaRepository<ShopNameChangeRequest, UUID> {

    @Query("""
            SELECT r FROM ShopNameChangeRequest r WHERE r.deleted = false
            AND r.shop.id = :shopId
            AND (:status IS NULL OR r.status = :status)
            ORDER BY r.createdAt DESC
            """)
    Page<ShopNameChangeRequest> findByShop(
            @Param("shopId") UUID shopId, @Param("status") RevokeRequestStatus status, Pageable pageable);

    @Query("""
            SELECT r FROM ShopNameChangeRequest r WHERE r.deleted = false
            AND (:status IS NULL OR r.status = :status)
            ORDER BY r.createdAt DESC
            """)
    Page<ShopNameChangeRequest> findAllByStatus(
            @Param("status") RevokeRequestStatus status, Pageable pageable);

    boolean existsByShop_IdAndStatusAndDeletedFalse(UUID shopId, RevokeRequestStatus status);

    Optional<ShopNameChangeRequest> findByIdAndDeletedFalse(UUID id);
}
