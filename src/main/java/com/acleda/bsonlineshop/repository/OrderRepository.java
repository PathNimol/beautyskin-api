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
import java.math.BigDecimal;
import java.time.Instant;
public interface OrderRepository extends JpaRepository<Order, UUID> {
    Optional<Order> findByIdAndDeletedFalse(UUID id);
    Optional<Order> findByOrderRefAndDeletedFalse(String orderRef);

    /**
     * @param customerId when non-null, restrict to orders for that customer (storefront history).
     * @param shopId     when non-null, restrict to that shop (merchant / admin scope).
     */
    @Query("""
            SELECT o FROM Order o WHERE o.deleted = false
            AND (:customerId IS NULL OR o.customerId = :customerId)
            AND (:shopId IS NULL OR o.shop.id = :shopId)
            AND (:status IS NULL OR o.status = :status)
            """)
    Page<Order> listScoped(
            @Param("customerId") UUID customerId,
            @Param("shopId") UUID shopId,
            @Param("status") OrderStatus status,
            Pageable pageable);

    @Query("""
            SELECT o FROM Order o WHERE o.deleted = false
            AND (:customerId IS NULL OR o.customerId = :customerId)
            AND (:shopId IS NULL OR o.shop.id = :shopId)
            AND (:status IS NULL OR o.status = :status)
            AND (LOWER(o.orderRef) LIKE LOWER(CONCAT('%', :search, '%'))
                 OR LOWER(o.customerName) LIKE LOWER(CONCAT('%', :search, '%'))
                 OR LOWER(o.customerEmail) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<Order> searchScopedWithTerm(
            @Param("customerId") UUID customerId,
            @Param("shopId") UUID shopId,
            @Param("status") OrderStatus status,
            @Param("search") String search,
            Pageable pageable);

    default Page<Order> searchScoped(
            UUID customerId, UUID shopId, OrderStatus status, String search, Pageable pageable) {
        if (search == null || search.isBlank()) {
            return listScoped(customerId, shopId, status, pageable);
        }
        return searchScopedWithTerm(customerId, shopId, status, search.trim(), pageable);
    }

    @Query("""
            SELECT COALESCE(SUM(o.total), 0) FROM Order o WHERE o.deleted = false
            AND o.status = com.acleda.bsonlineshop.enums.OrderStatus.DELIVERED
            AND (:shopId IS NULL OR o.shop.id = :shopId)
            AND o.createdAt >= :from AND o.createdAt < :to
            """)
    BigDecimal sumDeliveredRevenue(
            @Param("shopId") UUID shopId, @Param("from") Instant from, @Param("to") Instant to);

    @Query("""
            SELECT COUNT(DISTINCT o.customerId) FROM Order o WHERE o.deleted = false
            AND (:shopId IS NULL OR o.shop.id = :shopId)
            AND o.createdAt >= :from AND o.createdAt < :to
            """)
    long countDistinctCustomers(
            @Param("shopId") UUID shopId, @Param("from") Instant from, @Param("to") Instant to);

}
