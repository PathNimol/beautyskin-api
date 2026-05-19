package com.acleda.bsonlineshop.repository;

import com.acleda.bsonlineshop.entity.ShopStaff;
import com.acleda.bsonlineshop.enums.AccountStatus;
import com.acleda.bsonlineshop.enums.ShopUserRole;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShopStaffRepository extends JpaRepository<ShopStaff, UUID> {

    @Query("""
            SELECT s FROM ShopStaff s WHERE s.deleted = false AND s.shop.id = :shopId
            AND (:role IS NULL OR s.role = :role)
            AND (:status IS NULL OR s.status = :status)
            AND (:search IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%'))
                 OR LOWER(s.email) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<ShopStaff> search(
            @Param("shopId") UUID shopId,
            @Param("role") ShopUserRole role,
            @Param("status") AccountStatus status,
            @Param("search") String search,
            Pageable pageable);
}
