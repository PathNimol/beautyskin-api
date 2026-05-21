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
            """)
    Page<ShopStaff> list(
            @Param("shopId") UUID shopId,
            @Param("role") ShopUserRole role,
            @Param("status") AccountStatus status,
            Pageable pageable);

    @Query("""
            SELECT s FROM ShopStaff s WHERE s.deleted = false AND s.shop.id = :shopId
            AND (:role IS NULL OR s.role = :role)
            AND (:status IS NULL OR s.status = :status)
            AND (LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%'))
                 OR LOWER(s.email) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<ShopStaff> searchWithTerm(
            @Param("shopId") UUID shopId,
            @Param("role") ShopUserRole role,
            @Param("status") AccountStatus status,
            @Param("search") String search,
            Pageable pageable);

    default Page<ShopStaff> search(
            UUID shopId, ShopUserRole role, AccountStatus status, String search, Pageable pageable) {
        if (search == null || search.isBlank()) {
            return list(shopId, role, status, pageable);
        }
        return searchWithTerm(shopId, role, status, search.trim(), pageable);
    }
}
