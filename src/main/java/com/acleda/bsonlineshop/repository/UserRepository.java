package com.acleda.bsonlineshop.repository;

import com.acleda.bsonlineshop.entity.User;
import com.acleda.bsonlineshop.enums.UserRole;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmailIgnoreCaseAndDeletedFalse(String email);
    boolean existsByEmailIgnoreCaseAndDeletedFalse(String email);

    @Query("""
        SELECT u FROM User u WHERE u.deleted = false AND u.role = :role
        AND (:search IS NULL
             OR LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :search, '%'))
             OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))
        """)
    Page<User> searchCustomers(@Param("role") UserRole role, @Param("search") String search, Pageable pageable);
}
