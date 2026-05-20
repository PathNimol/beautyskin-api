package com.acleda.bsonlineshop.repository;

import com.acleda.bsonlineshop.entity.Review;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    Page<Review> findByProduct_IdAndDeletedFalseOrderByCreatedAtDesc(UUID productId, Pageable pageable);
}
