package com.acleda.bsonlineshop.repository;

import com.acleda.bsonlineshop.entity.Order;
import com.acleda.bsonlineshop.enums.OrderStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    Optional<Order> findByIdAndDeletedFalse(UUID id);
    Optional<Order> findByOrderRefAndDeletedFalse(String orderRef);

    @Query("""
            SELECT o FROM Order o WHERE o.deleted = false
            AND (:shopId IS NULL OR o.shop.id = :shopId)
            AND (:status IS NULL OR o.status = :status)
            AND (:search IS NULL OR LOWER(o.orderRef) LIKE LOWER(CONCAT('%', :search, '%'))
                 OR LOWER(o.customerName) LIKE LOWER(CONCAT('%', :search, '%'))
                 OR LOWER(o.customerEmail) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<Order> search(
            @Param("shopId") UUID shopId,
            @Param("status") OrderStatus status,
            @Param("search") String search,
            Pageable pageable);
}
