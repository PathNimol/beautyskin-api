package com.acleda.bsonlineshop.repository;

import com.acleda.bsonlineshop.entity.ShopBrand;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopBrandRepository extends JpaRepository<ShopBrand, UUID> {
    List<ShopBrand> findByShopIdAndDeletedFalse(UUID shopId);
}
