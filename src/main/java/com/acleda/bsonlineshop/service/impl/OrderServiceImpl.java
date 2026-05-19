package com.acleda.bsonlineshop.service.impl;

import com.acleda.bsonlineshop.dto.cart.CartResponse;
import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.dto.order.BulkOrderStatusRequest;
import com.acleda.bsonlineshop.dto.order.OrderResponse;
import com.acleda.bsonlineshop.dto.order.PlaceOrderRequest;
import com.acleda.bsonlineshop.entity.Order;
import com.acleda.bsonlineshop.entity.OrderLine;
import com.acleda.bsonlineshop.entity.Shop;
import com.acleda.bsonlineshop.entity.User;
import com.acleda.bsonlineshop.enums.OrderStatus;
import com.acleda.bsonlineshop.enums.PaymentStatus;
import com.acleda.bsonlineshop.exception.BusinessException;
import com.acleda.bsonlineshop.exception.ResourceNotFoundException;
import com.acleda.bsonlineshop.mapper.OrderMapper;
import com.acleda.bsonlineshop.repository.OrderRepository;
import com.acleda.bsonlineshop.repository.ShopRepository;
import com.acleda.bsonlineshop.repository.UserRepository;
import com.acleda.bsonlineshop.security.SecurityUtils;
import com.acleda.bsonlineshop.service.CartService;
import com.acleda.bsonlineshop.service.OrderService;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final CartService cartService;
    private final OrderMapper orderMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> list(UUID shopId, String status, String search, int page, int limit) {
        UUID scoped = SecurityUtils.requireShopId(shopId);
        OrderStatus orderStatus = parseStatus(status);
        Page<Order> result = orderRepository.search(
                scoped, orderStatus, search, PageRequest.of(Math.max(page - 1, 0), limit));
        return PageResponse.from(result.map(orderMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getById(UUID id) {
        return orderMapper.toResponse(findOrder(id));
    }

    @Override
    @Transactional
    public OrderResponse placeOrder(PlaceOrderRequest request) {
        CartResponse cart = cartService.getCart();
        if (cart.getItems().isEmpty()) {
            throw new BusinessException("Cart is empty");
        }
        Map<UUID, java.util.List<com.acleda.bsonlineshop.dto.cart.CartItemResponse>> byShop =
                cart.getItems().stream().collect(Collectors.groupingBy(
                        i -> i.getShopId() != null ? i.getShopId() : UUID.randomUUID()));
        Order last = null;
        User user = userRepository.findById(SecurityUtils.currentUserId()).orElse(null);
        for (var entry : byShop.entrySet()) {
            Shop shop = shopRepository.findByIdAndDeletedFalse(entry.getKey())
                    .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));
            Order order = new Order();
            order.setOrderRef("ORD-" + System.currentTimeMillis());
            if (user != null) {
                order.setCustomerId(user.getId());
                order.setCustomerName(user.getFullName());
                order.setCustomerEmail(user.getEmail());
                order.setCustomerPhone(user.getPhone());
                order.setCustomerAvatar(user.getAvatar());
                order.setCustomerAvatarAlt(user.getAvatarAlt());
            } else {
                order.setCustomerName(request.getFirstName() + " " + request.getLastName());
                order.setCustomerEmail(request.getEmail());
                order.setCustomerPhone(request.getPhone());
            }
            order.setShop(shop);
            order.setShopName(shop.getName());
            BigDecimal subtotal = BigDecimal.ZERO;
            for (var item : entry.getValue()) {
                OrderLine line = new OrderLine();
                line.setOrder(order);
                line.setProductId(item.getProductId());
                line.setName(item.getName());
                line.setBrand(item.getBrand());
                line.setQty(item.getQuantity());
                line.setPrice(item.getPrice());
                line.setImage(item.getImage());
                line.setImageAlt(item.getImageAlt());
                order.getItems().add(line);
                subtotal = subtotal.add(item.getLineTotal());
            }
            BigDecimal discount = subtotal.multiply(BigDecimal.valueOf(0.10));
            order.setSubtotal(subtotal);
            order.setDiscount(discount);
            order.setShipping(BigDecimal.ZERO);
            order.setTotal(subtotal.subtract(discount));
            order.setStatus(OrderStatus.PENDING);
            order.setPaymentMethod(request.getPaymentMethod());
            order.setPaymentStatus(PaymentStatus.PENDING);
            order.setAddress(request.getAddress());
            order.setCity(request.getCity());
            order.setState(request.getState());
            order.setZip(request.getZip());
            order.setCountry(request.getCountry());
            order.setNotes(request.getNotes());
            last = orderRepository.save(order);
        }
        cartService.clear();
        return orderMapper.toResponse(last);
    }

    @Override
    @Transactional
    public OrderResponse updateStatus(UUID id, OrderStatus status) {
        Order order = findOrder(id);
        order.setStatus(status);
        return orderMapper.toResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    public void bulkUpdateStatus(BulkOrderStatusRequest request) {
        for (UUID id : request.getIds()) {
            updateStatus(id, request.getStatus());
        }
    }

    private Order findOrder(UUID id) {
        return orderRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    private OrderStatus parseStatus(String status) {
        if (status == null || status.isBlank()) return null;
        return OrderStatus.valueOf(status.toUpperCase());
    }
}
