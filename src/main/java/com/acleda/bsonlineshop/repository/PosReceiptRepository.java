package com.acleda.bsonlineshop.repository;

import com.acleda.bsonlineshop.entity.PosReceipt;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PosReceiptRepository extends JpaRepository<PosReceipt, UUID> {

    @Query("""
            SELECT r FROM PosReceipt r WHERE r.deleted = false AND r.shop.id = :shopId
            AND r.createdAt >= :since
            ORDER BY r.createdAt DESC
            """)
    Page<PosReceipt> findRecentByShop(
            @Param("shopId") UUID shopId, @Param("since") Instant since, Pageable pageable);

    long countByShop_IdAndCancelledTrueAndCreatedAtAfterAndDeletedFalse(UUID shopId, Instant since);
}
