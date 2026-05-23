package com.acleda.bsonlineshop.repository;

import com.acleda.bsonlineshop.entity.Product;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {
    Optional<Product> findByIdAndDeletedFalse(UUID id);

    boolean existsBySkuAndDeletedFalse(String sku);

    Optional<Product> findBySkuAndDeletedFalse(String sku);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id AND p.deleted = false")
    Optional<Product> findByIdAndDeletedFalseForUpdate(@Param("id") UUID id);

    @Query("""
            SELECT p FROM Product p WHERE p.deleted = false
            ORDER BY p.sold DESC, p.name ASC
            """)
    Page<Product> findTopBySold(Pageable pageable);

    @Query("""
            SELECT p FROM Product p WHERE p.deleted = false
            AND p.expiryDate IS NOT NULL AND p.expiryDate < :today
            ORDER BY p.expiryDate ASC
            """)
    Page<Product> findExpiredBefore(@Param("today") LocalDate today, Pageable pageable);

    @Query("""
            SELECT p.category, COALESCE(SUM(p.sold), 0) FROM Product p
            WHERE p.deleted = false AND p.category IS NOT NULL AND TRIM(p.category) <> ''
            GROUP BY p.category
            ORDER BY SUM(p.sold) DESC
            """)
    List<Object[]> sumSoldByCategory();
}
