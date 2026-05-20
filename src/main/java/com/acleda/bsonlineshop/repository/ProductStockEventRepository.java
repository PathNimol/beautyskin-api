package com.acleda.bsonlineshop.repository;

import com.acleda.bsonlineshop.entity.ProductStockEvent;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductStockEventRepository extends JpaRepository<ProductStockEvent, UUID> {
    List<ProductStockEvent> findByProductIdAndDeletedFalseOrderByCreatedAtDesc(UUID productId);
}
