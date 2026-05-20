package com.acleda.bsonlineshop.repository;

import com.acleda.bsonlineshop.entity.Supplier;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SupplierRepository extends JpaRepository<Supplier, UUID> {

    @Query("""
            SELECT s FROM Supplier s WHERE s.deleted = false
            AND (:search IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%'))
                 OR LOWER(s.contactPerson) LIKE LOWER(CONCAT('%', :search, '%'))
                 OR LOWER(s.country) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<Supplier> search(@Param("search") String search, Pageable pageable);
}
