package com.acleda.bsonlineshop.repository;

import com.acleda.bsonlineshop.entity.InventoryItem;
import com.acleda.bsonlineshop.enums.InventoryStatus;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, UUID> {
    Optional<InventoryItem> findByIdAndDeletedFalse(UUID id);

    @Query("""
            SELECT i FROM InventoryItem i WHERE i.deleted = false
            AND i.shop.id = :shopId
            AND (:status IS NULL OR i.invStatus = :status)
            """)
    Page<InventoryItem> list(
            @Param("shopId") UUID shopId, @Param("status") InventoryStatus status, Pageable pageable);

    @Query("""
            SELECT i FROM InventoryItem i WHERE i.deleted = false
            AND i.shop.id = :shopId
            AND (:status IS NULL OR i.invStatus = :status)
            AND (LOWER(i.productName) LIKE LOWER(CONCAT('%', :search, '%'))
                 OR LOWER(i.sku) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<InventoryItem> searchWithTerm(
            @Param("shopId") UUID shopId,
            @Param("status") InventoryStatus status,
            @Param("search") String search,
            Pageable pageable);

    default Page<InventoryItem> search(
            UUID shopId, InventoryStatus status, String search, Pageable pageable) {
        if (search == null || search.isBlank()) {
            return list(shopId, status, pageable);
        }
        return searchWithTerm(shopId, status, search.trim(), pageable);
    }

    @Query("""
            SELECT i FROM InventoryItem i WHERE i.deleted = false
            AND (:shopId IS NULL OR i.shop.id = :shopId)
            AND (:status IS NULL OR i.invStatus = :status)
            """)
    Page<InventoryItem> listPlatform(
            @Param("shopId") UUID shopId, @Param("status") InventoryStatus status, Pageable pageable);

    @Query("""
            SELECT i FROM InventoryItem i WHERE i.deleted = false
            AND (:shopId IS NULL OR i.shop.id = :shopId)
            AND (:status IS NULL OR i.invStatus = :status)
            AND (LOWER(i.productName) LIKE LOWER(CONCAT('%', :search, '%'))
                 OR LOWER(i.sku) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<InventoryItem> searchPlatformWithTerm(
            @Param("shopId") UUID shopId,
            @Param("status") InventoryStatus status,
            @Param("search") String search,
            Pageable pageable);

    default Page<InventoryItem> searchPlatform(
            UUID shopId, InventoryStatus status, String search, Pageable pageable) {
        if (search == null || search.isBlank()) {
            return listPlatform(shopId, status, pageable);
        }
        return searchPlatformWithTerm(shopId, status, search.trim(), pageable);
    }

    @Query("""
            SELECT i FROM InventoryItem i WHERE i.deleted = false
            AND i.invStatus IN :statuses
            ORDER BY i.currentStock ASC
            """)
    Page<InventoryItem> findPlatformByStatuses(
            @Param("statuses") Collection<InventoryStatus> statuses, Pageable pageable);
}
