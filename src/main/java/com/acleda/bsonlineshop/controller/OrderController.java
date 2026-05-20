package com.acleda.bsonlineshop.controller;

import com.acleda.bsonlineshop.dto.common.ApiResponse;
import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.dto.order.BulkOrderStatusRequest;
import com.acleda.bsonlineshop.dto.order.OrderResponse;
import com.acleda.bsonlineshop.dto.order.PlaceOrderRequest;
import com.acleda.bsonlineshop.enums.OrderStatus;
import com.acleda.bsonlineshop.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "List orders", description = "Paginated orders for the caller's scope. Merchants pass shopId; filters by status and search.")
    @GetMapping
    public ApiResponse<PageResponse<OrderResponse>> list(
            @RequestParam(required = false) UUID shopId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.success(orderService.list(shopId, status, search, page, limit));
    }

    @Operation(summary = "Get order", description = "Return one order by id if the caller is allowed to view it.")
    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> get(@PathVariable UUID id) {
        return ApiResponse.success(orderService.getById(id));
    }

    @Operation(summary = "Place order", description = "Create an order from the current cart and shipping details (PlaceOrderRequest).")
    @PostMapping
    @PreAuthorize("@authz.storeOrderActor()")
    public ApiResponse<OrderResponse> place(@Valid @RequestBody PlaceOrderRequest request) {
        return ApiResponse.success("Order placed", orderService.placeOrder(request));
    }

    @Operation(summary = "Update order status", description = "Set a single order's status (body: { \"status\": \"SHIPPED\" } enum name).")
    @PatchMapping("/{id}/status")
    @PreAuthorize("@authz.adminOrMerchant()")
    public ApiResponse<OrderResponse> updateStatus(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        OrderStatus status = OrderStatus.valueOf(body.get("status").toUpperCase());
        return ApiResponse.success(orderService.updateStatus(id, status));
    }

    @Operation(summary = "Bulk update order status", description = "Apply the same status to multiple order ids in one request.")
    @PatchMapping("/bulk")
    @PreAuthorize("@authz.adminOrMerchant()")
    public ApiResponse<Void> bulk(@Valid @RequestBody BulkOrderStatusRequest request) {
        orderService.bulkUpdateStatus(request);
        return ApiResponse.success("Orders updated", null);
    }
}
