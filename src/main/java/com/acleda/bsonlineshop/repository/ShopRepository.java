package com.acleda.bsonlineshop.repository;

import com.acleda.bsonlineshop.entity.Shop;
import com.acleda.bsonlineshop.enums.ShopStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShopRepository extends JpaRepository<Shop, UUID> {
    Optional<Shop> findByIdAndDeletedFalse(UUID id);

    @Query("""
            SELECT s FROM Shop s WHERE s.deleted = false
            AND (:status IS NULL OR s.status = :status)
            AND (:search IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%'))
                 OR LOWER(s.ownerName) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<Shop> search(@Param("status") ShopStatus status, @Param("search") String search, Pageable pageable);
}
