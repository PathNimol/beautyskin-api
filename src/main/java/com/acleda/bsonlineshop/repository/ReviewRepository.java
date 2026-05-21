package com.acleda.bsonlineshop.repository;

import com.acleda.bsonlineshop.entity.Review;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    Page<Review> findByProduct_IdAndDeletedFalseOrderByCreatedAtDesc(UUID productId, Pageable pageable);

    Optional<Review> findByIdAndDeletedFalse(UUID id);

    long countByProduct_IdAndDeletedFalse(UUID productId);

    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r WHERE r.product.id = :productId AND r.deleted = false")
    double averageRatingByProduct(@Param("productId") UUID productId);
}
