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

    Optional<Shop> findBySlugAndDeletedFalse(String slug);

    @Query("""
            SELECT s FROM Shop s WHERE s.deleted = false
            AND (:status IS NULL OR s.status = :status)
            """)
    Page<Shop> list(@Param("status") ShopStatus status, Pageable pageable);

    @Query("""
            SELECT s FROM Shop s WHERE s.deleted = false
            AND (:status IS NULL OR s.status = :status)
            AND (LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%'))
                 OR LOWER(s.ownerName) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<Shop> searchWithTerm(
            @Param("status") ShopStatus status, @Param("search") String search, Pageable pageable);

    default Page<Shop> search(ShopStatus status, String search, Pageable pageable) {
        if (search == null || search.isBlank()) {
            return list(status, pageable);
        }
        return searchWithTerm(status, search.trim(), pageable);
    }
}
