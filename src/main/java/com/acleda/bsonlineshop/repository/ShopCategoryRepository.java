package com.acleda.bsonlineshop.repository;

import com.acleda.bsonlineshop.entity.ShopCategory;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopCategoryRepository extends JpaRepository<ShopCategory, UUID> {
    List<ShopCategory> findByShopIdAndDeletedFalse(UUID shopId);
}
