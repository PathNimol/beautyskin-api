package com.acleda.bsonlineshop.service.impl;

import com.acleda.bsonlineshop.enums.InventoryStatus;
import com.acleda.bsonlineshop.enums.OrderStatus;
import com.acleda.bsonlineshop.mapper.OrderMapper;
import com.acleda.bsonlineshop.repository.InventoryItemRepository;
import com.acleda.bsonlineshop.repository.OrderRepository;
import com.acleda.bsonlineshop.repository.ProductRepository;
import com.acleda.bsonlineshop.repository.ShopRepository;
import com.acleda.bsonlineshop.repository.UserRepository;
import com.acleda.bsonlineshop.security.SecurityUtils;
import com.acleda.bsonlineshop.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ShopRepository shopRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final OrderMapper orderMapper;

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> adminDashboard() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalShops", shopRepository.count());
        data.put("totalOrders", orderRepository.count());
        data.put("totalProducts", productRepository.count());
        data.put("totalCustomers", userRepository.count());
        data.put(
                "recentOrders",
                orderRepository.searchScoped(null, null, null, null, PageRequest.of(0, 5)).getContent().stream()
                        .map(orderMapper::toResponse)
                        .toList());
        data.put(
                "pendingOrders",
                orderRepository
                        .searchScoped(null, null, OrderStatus.PENDING, null, PageRequest.of(0, 1))
                        .getTotalElements());
        return data;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> shopDashboard(UUID shopId) {
        UUID scoped = SecurityUtils.requireShopId(shopId);
        Map<String, Object> data = new HashMap<>();
        data.put("shopId", scoped);
        data.put(
                "orders",
                orderRepository.searchScoped(null, scoped, null, null, PageRequest.of(0, 1)).getTotalElements());
        data.put("products", countShopProducts(scoped));
        data.put("lowStockItems", inventoryItemRepository.search(scoped, InventoryStatus.LOW, null, PageRequest.of(0, 5)).getContent());
        Instant to = Instant.now();
        Instant from = to.minus(30, ChronoUnit.DAYS);
        data.put("revenue", orderRepository.sumDeliveredRevenue(scoped, from, to));
        data.put(
                "recentOrders",
                orderRepository.searchScoped(null, scoped, null, null, PageRequest.of(0, 5)).getContent().stream()
                        .map(orderMapper::toResponse)
                        .toList());
        return data;
    }

    private long countShopProducts(UUID shopId) {
        Specification<com.acleda.bsonlineshop.entity.Product> spec = (root, query, cb) ->
                cb.and(
                        cb.isFalse(root.get("deleted")),
                        cb.equal(root.get("shop").get("id"), shopId)
                );
        return productRepository.count(spec);
    }
}