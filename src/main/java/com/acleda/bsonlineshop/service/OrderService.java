package com.acleda.bsonlineshop.service;

import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.dto.order.BulkOrderStatusRequest;
import com.acleda.bsonlineshop.dto.order.OrderResponse;
import com.acleda.bsonlineshop.dto.order.PlaceOrderRequest;
import com.acleda.bsonlineshop.enums.OrderStatus;
import java.util.UUID;

public interface OrderService {
    PageResponse<OrderResponse> list(UUID shopId, String status, String search, int page, int limit);
    OrderResponse getById(UUID id);
    OrderResponse placeOrder(PlaceOrderRequest request);
    OrderResponse updateStatus(UUID id, OrderStatus status);
    void bulkUpdateStatus(BulkOrderStatusRequest request);
}
