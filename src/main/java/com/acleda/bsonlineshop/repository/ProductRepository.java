package com.acleda.bsonlineshop.repository;

import com.acleda.bsonlineshop.entity.Product;
import com.acleda.bsonlineshop.enums.ProductStatus;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {
    Optional<Product> findByIdAndDeletedFalse(UUID id);

}
