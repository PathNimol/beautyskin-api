package com.acleda.bsonlineshop.service.impl;

import com.acleda.bsonlineshop.enums.OrderStatus;
import com.acleda.bsonlineshop.repository.OrderRepository;
import com.acleda.bsonlineshop.security.SecurityUtils;
import com.acleda.bsonlineshop.service.AnalyticsService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final OrderRepository orderRepository;

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> summary(UUID shopId, String range) {
        UUID scoped = SecurityUtils.requireShopId(shopId);
        Instant to = Instant.now();
        Instant from = to.minus(parseDays(range), ChronoUnit.DAYS);

        Map<String, Object> data = new HashMap<>();
        long totalOrders = orderRepository
                .searchScoped(null, scoped, null, null, PageRequest.of(0, 1))
                .getTotalElements();
        long pending = orderRepository
                .searchScoped(null, scoped, OrderStatus.PENDING, null, PageRequest.of(0, 1))
                .getTotalElements();
        long delivered = orderRepository
                .searchScoped(null, scoped, OrderStatus.DELIVERED, null, PageRequest.of(0, 1))
                .getTotalElements();
        BigDecimal revenue = orderRepository.sumDeliveredRevenue(scoped, from, to);
        long customers = orderRepository.countDistinctCustomers(scoped, from, to);

        data.put("range", range);
        data.put("from", from);
        data.put("to", to);
        data.put("totalOrders", totalOrders);
        data.put("pendingOrders", pending);
        data.put("deliveredOrders", delivered);
        data.put("revenue", revenue);
        data.put("customers", customers);
        return data;
    }

    private static long parseDays(String range) {
        if (range == null || range.isBlank()) return 30;
        String r = range.trim().toLowerCase();
        if (r.endsWith("d")) {
            try {
                return Long.parseLong(r.substring(0, r.length() - 1));
            } catch (NumberFormatException ignored) {
                return 30;
            }
        }
        return 30;
    }
}
