package com.acleda.bsonlineshop.repository;

import com.acleda.bsonlineshop.entity.Supplier;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SupplierRepository extends JpaRepository<Supplier, UUID> {
    java.util.Optional<Supplier> findByIdAndDeletedFalse(UUID id);

    @Query("SELECT s FROM Supplier s WHERE s.deleted = false")
    Page<Supplier> list(Pageable pageable);

    @Query("""
            SELECT s FROM Supplier s WHERE s.deleted = false
            AND (LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%'))
                 OR LOWER(s.contactPerson) LIKE LOWER(CONCAT('%', :search, '%'))
                 OR LOWER(s.country) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<Supplier> searchWithTerm(@Param("search") String search, Pageable pageable);

    default Page<Supplier> search(String search, Pageable pageable) {
        if (search == null || search.isBlank()) {
            return list(pageable);
        }
        return searchWithTerm(search.trim(), pageable);
    }
}
