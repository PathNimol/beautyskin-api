package com.acleda.bsonlineshop.dto.order;

import com.acleda.bsonlineshop.enums.OrderStatus;
import com.acleda.bsonlineshop.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderResponse {
    private UUID id;
    private String orderRef;
    private UUID customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private UUID shopId;
    private String shopName;
    private List<OrderLineResponse> items;
    private BigDecimal total;
    private BigDecimal subtotal;
    private BigDecimal shipping;
    private BigDecimal discount;
    private OrderStatus status;
    private String paymentMethod;
    private PaymentStatus paymentStatus;
    private String address;
    private String city;
    private String state;
    private String country;
    private String zip;
    private String trackingNumber;
    private String notes;
    private Instant createdAt;
    private Instant updatedAt;
}
