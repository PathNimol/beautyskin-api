package com.acleda.bsonlineshop.repository;

import com.acleda.bsonlineshop.entity.Promotion;
import com.acleda.bsonlineshop.enums.PromotionStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PromotionRepository extends JpaRepository<Promotion, UUID> {
    Optional<Promotion> findByCodeIgnoreCaseAndDeletedFalse(String code);

    @Query("""
            SELECT p FROM Promotion p WHERE p.deleted = false
            AND (:shopId IS NULL OR p.shop.id = :shopId)
            AND (:status IS NULL OR p.status = :status)
            AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))
                 OR LOWER(p.code) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<Promotion> search(
            @Param("shopId") UUID shopId,
            @Param("status") PromotionStatus status,
            @Param("search") String search,
            Pageable pageable);
}
