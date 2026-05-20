package com.acleda.bsonlineshop.controller;

import com.acleda.bsonlineshop.dto.common.ApiResponse;
import com.acleda.bsonlineshop.enums.OrderStatus;
import com.acleda.bsonlineshop.repository.OrderRepository;
import com.acleda.bsonlineshop.security.SecurityUtils;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("@authz.adminOrMerchant()")
public class AnalyticsController {

    private final OrderRepository orderRepository;

    @Operation(summary = "Analytics summary", description = "Order counts and placeholder revenue for a shop scope (shopId optional for admin). Range query param reserved for future use.")
    @GetMapping("/summary")
    public ApiResponse<Map<String, Object>> summary(
            @RequestParam(required = false) UUID shopId,
            @RequestParam(defaultValue = "30d") String range) {
        UUID scoped = SecurityUtils.requireShopId(shopId);
        Map<String, Object> data = new HashMap<>();
        long totalOrders = orderRepository.search(scoped, null, null, PageRequest.of(0, 1)).getTotalElements();
        long pending = orderRepository.search(scoped, OrderStatus.PENDING, null, PageRequest.of(0, 1)).getTotalElements();
        long delivered = orderRepository.search(scoped, OrderStatus.DELIVERED, null, PageRequest.of(0, 1)).getTotalElements();
        data.put("range", range);
        data.put("totalOrders", totalOrders);
        data.put("pendingOrders", pending);
        data.put("deliveredOrders", delivered);
        data.put("revenue", BigDecimal.ZERO);
        data.put("customers", 0);
        return ApiResponse.success(data);
    }
}
