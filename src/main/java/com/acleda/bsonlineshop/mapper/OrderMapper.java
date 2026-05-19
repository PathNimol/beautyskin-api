package com.acleda.bsonlineshop.mapper;

import com.acleda.bsonlineshop.dto.order.OrderLineResponse;
import com.acleda.bsonlineshop.dto.order.OrderResponse;
import com.acleda.bsonlineshop.entity.Order;
import com.acleda.bsonlineshop.entity.OrderLine;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order o) {
        return OrderResponse.builder()
                .id(o.getId())
                .orderRef(o.getOrderRef())
                .customerId(o.getCustomerId())
                .customerName(o.getCustomerName())
                .customerEmail(o.getCustomerEmail())
                .customerPhone(o.getCustomerPhone())
                .shopId(o.getShop().getId())
                .shopName(o.getShopName())
                .items(o.getItems().stream().map(this::toLine).toList())
                .total(o.getTotal())
                .subtotal(o.getSubtotal())
                .shipping(o.getShipping())
                .discount(o.getDiscount())
                .status(o.getStatus())
                .paymentMethod(o.getPaymentMethod())
                .paymentStatus(o.getPaymentStatus())
                .address(o.getAddress())
                .city(o.getCity())
                .state(o.getState())
                .country(o.getCountry())
                .zip(o.getZip())
                .trackingNumber(o.getTrackingNumber())
                .notes(o.getNotes())
                .createdAt(o.getCreatedAt())
                .updatedAt(o.getUpdatedAt())
                .build();
    }

    private OrderLineResponse toLine(OrderLine line) {
        return OrderLineResponse.builder()
                .productId(line.getProductId())
                .name(line.getName())
                .brand(line.getBrand())
                .qty(line.getQty())
                .price(line.getPrice())
                .image(line.getImage())
                .imageAlt(line.getImageAlt())
                .build();
    }
}
