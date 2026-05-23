package com.acleda.bsonlineshop.service.impl;

import com.acleda.bsonlineshop.dto.cart.CartResponse;
import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.dto.order.BulkOrderStatusRequest;
import com.acleda.bsonlineshop.dto.order.OrderResponse;
import com.acleda.bsonlineshop.dto.order.PlaceOrderRequest;
import com.acleda.bsonlineshop.dto.order.PlaceOrderResult;
import com.acleda.bsonlineshop.entity.Order;
import com.acleda.bsonlineshop.entity.OrderLine;
import com.acleda.bsonlineshop.entity.Product;
import com.acleda.bsonlineshop.entity.Shop;
import com.acleda.bsonlineshop.entity.User;
import com.acleda.bsonlineshop.enums.OrderStatus;
import com.acleda.bsonlineshop.enums.PaymentStatus;
import com.acleda.bsonlineshop.enums.ProductStatus;
import com.acleda.bsonlineshop.enums.UserRole;
import com.acleda.bsonlineshop.exception.BusinessException;
import com.acleda.bsonlineshop.exception.ResourceNotFoundException;
import com.acleda.bsonlineshop.mapper.OrderMapper;
import com.acleda.bsonlineshop.repository.OrderRepository;
import com.acleda.bsonlineshop.repository.ProductRepository;
import com.acleda.bsonlineshop.repository.PromotionRepository;
import com.acleda.bsonlineshop.repository.ShopRepository;
import com.acleda.bsonlineshop.repository.UserRepository;
import com.acleda.bsonlineshop.security.SecurityUtils;
import com.acleda.bsonlineshop.security.UserPrincipal;
import com.acleda.bsonlineshop.service.CartService;
import com.acleda.bsonlineshop.service.OrderService;
import com.acleda.bsonlineshop.service.StockEventHelper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PromotionRepository promotionRepository;
    private final CartService cartService;
    private final OrderMapper orderMapper;
    private final StockEventHelper stockEventHelper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> list(UUID shopId, String status, String search, int page, int limit) {
        UserPrincipal principal = SecurityUtils.currentUser();
        OrderStatus orderStatus = parseStatus(status);
        Pageable pg = PageRequest.of(Math.max(page - 1, 0), limit);
        Page<Order> result =
                switch (principal.getRole()) {
                    case CUSTOMER -> orderRepository.searchScoped(
                            principal.getId(), null, orderStatus, search, pg);
                    case ADMIN -> orderRepository.searchScoped(null, shopId, orderStatus, search, pg);
                    case OWNER, STAFF -> {
                        UUID scoped = SecurityUtils.requireShopId(shopId);
                        yield orderRepository.searchScoped(null, scoped, orderStatus, search, pg);
                    }
                };
        return PageResponse.from(result.map(orderMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getById(UUID id) {
        Order order = findOrder(id);
        assertCanView(order);
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional
    public PlaceOrderResult placeOrder(PlaceOrderRequest request) {
        CartResponse cart = cartService.getCart();
        if (cart.getItems().isEmpty()) {
            throw new BusinessException("Cart is empty");
        }

        for (var line : cart.getItems()) {
            Product product =
                    productRepository.findByIdAndDeletedFalseForUpdate(line.getProductId()).orElseThrow(
                            () -> new ResourceNotFoundException("Product not found: " + line.getProductId()));
            assertPurchasable(product);
            if (product.getStock() < line.getQuantity()) {
                throw new BusinessException("Insufficient stock for " + product.getName());
            }
            if (!Objects.equals(product.getShop().getId(), line.getShopId())) {
                throw new BusinessException("Cart shop mismatch for " + product.getName());
            }
        }

        Map<UUID, List<com.acleda.bsonlineshop.dto.cart.CartItemResponse>> byShop =
                cart.getItems().stream().collect(Collectors.groupingBy(com.acleda.bsonlineshop.dto.cart.CartItemResponse::getShopId));

        for (UUID sid : byShop.keySet()) {
            if (sid == null) {
                throw new BusinessException("Cart line missing shop scope");
            }
            shopRepository.findByIdAndDeletedFalse(sid).orElseThrow(() -> new ResourceNotFoundException("Shop not found"));
        }

        BigDecimal cartSubtotal = cart.getSubtotal();
        BigDecimal cartDiscount = cart.getDiscount();

        User user =
                userRepository.findById(SecurityUtils.currentUserId()).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<ShopBucket> buckets = new ArrayList<>();
        for (var entry : byShop.entrySet()) {
            BigDecimal shopSub = entry.getValue().stream()
                    .map(com.acleda.bsonlineshop.dto.cart.CartItemResponse::getLineTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            buckets.add(new ShopBucket(entry.getKey(), entry.getValue(), shopSub));
        }

        BigDecimal allocatedDiscount = BigDecimal.ZERO;
        List<Order> persisted = new ArrayList<>();
        for (int i = 0; i < buckets.size(); i++) {
            ShopBucket bucket = buckets.get(i);
            BigDecimal shopDiscount;
            if (i == buckets.size() - 1) {
                shopDiscount = cartDiscount.subtract(allocatedDiscount).max(BigDecimal.ZERO);
            } else {
                if (cartSubtotal.signum() == 0) {
                    shopDiscount = BigDecimal.ZERO;
                } else {
                    shopDiscount = cartDiscount
                            .multiply(bucket.subtotal())
                            .divide(cartSubtotal, 2, RoundingMode.HALF_UP)
                            .max(BigDecimal.ZERO);
                }
                allocatedDiscount = allocatedDiscount.add(shopDiscount);
            }

            Shop shop =
                    shopRepository.findByIdAndDeletedFalse(bucket.shopId()).orElseThrow(() -> new ResourceNotFoundException("Shop not found"));

            Order order = new Order();
            order.setOrderRef(newOrderRef());
            order.setCustomerId(user.getId());
            order.setCustomerName(request.getFirstName().trim() + " " + request.getLastName().trim());
            order.setCustomerEmail(StringUtils.hasText(request.getEmail()) ? request.getEmail() : user.getEmail());
            order.setCustomerPhone(StringUtils.hasText(request.getPhone()) ? request.getPhone() : user.getPhone());
            order.setCustomerAvatar(user.getAvatar());
            order.setCustomerAvatarAlt(user.getAvatarAlt());
            order.setShop(shop);
            order.setShopName(shop.getName());

            BigDecimal subtotalLines = bucket.subtotal();
            for (var item : bucket.items()) {
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
            }

            order.setSubtotal(subtotalLines);
            order.setDiscount(shopDiscount);
            order.setShipping(BigDecimal.ZERO);
            order.setTotal(subtotalLines.subtract(shopDiscount).max(BigDecimal.ZERO));
            order.setStatus(OrderStatus.PENDING);
            order.setPaymentMethod(request.getPaymentMethod());
            order.setPaymentStatus(PaymentStatus.PENDING);
            order.setAddress(request.getAddress());
            order.setCity(request.getCity());
            order.setState(request.getState());
            order.setZip(request.getZip());
            order.setCountry(request.getCountry());
            order.setNotes(request.getNotes());

            persisted.add(orderRepository.save(order));
        }

        for (var line : cart.getItems()) {
            Product product = productRepository
                    .findByIdAndDeletedFalseForUpdate(line.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
            product.setStock(product.getStock() - line.getQuantity());
            product.setSold(product.getSold() + line.getQuantity());
            productRepository.save(product);
            stockEventHelper.record(product, -line.getQuantity(), "order_placed");
        }

        if (StringUtils.hasText(cart.getAppliedPromoCode())) {
            promotionRepository
                    .findByCodeIgnoreCaseAndDeletedFalse(cart.getAppliedPromoCode())
                    .ifPresent(promo -> {
                        promo.setUsedCount(promo.getUsedCount() + 1);
                        promotionRepository.save(promo);
                    });
        }

        cartService.clear();

        List<OrderResponse> responses = persisted.stream().map(orderMapper::toResponse).toList();
        return PlaceOrderResult.builder().orders(responses).build();
    }

    private static void assertPurchasable(Product product) {
        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new BusinessException("Product is not available: " + product.getName());
        }
        if (!product.isVisible() || product.isRevoked()) {
            throw new BusinessException("Product is not available: " + product.getName());
        }
    }

    private static String newOrderRef() {
        return "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private record ShopBucket(UUID shopId, List<com.acleda.bsonlineshop.dto.cart.CartItemResponse> items, BigDecimal subtotal) {}

    @Override
    @Transactional
    public OrderResponse updateStatus(UUID id, OrderStatus status) {
        Order order = findOrder(id);
        assertCanManage(order);
        OrderStatus previous = order.getStatus();
        order.setStatus(status);
        if (status == OrderStatus.CANCELLED && previous != OrderStatus.CANCELLED) {
            restoreStockForOrder(order);
        }
        return orderMapper.toResponse(orderRepository.save(order));
    }

    private void restoreStockForOrder(Order order) {
        for (OrderLine line : order.getItems()) {
            productRepository.findByIdAndDeletedFalseForUpdate(line.getProductId()).ifPresent(product -> {
                product.setStock(product.getStock() + line.getQty());
                product.setSold(Math.max(0, product.getSold() - line.getQty()));
                productRepository.save(product);
                stockEventHelper.record(product, line.getQty(), "order_cancelled");
            });
        }
    }

    @Override
    @Transactional
    public void bulkUpdateStatus(BulkOrderStatusRequest request) {
        for (UUID id : request.getIds()) {
            updateStatus(id, request.getStatus());
        }
    }

    private Order findOrder(UUID id) {
        return orderRepository.findByIdAndDeletedFalse(id).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    private void assertCanView(Order order) {
        UserPrincipal p = SecurityUtils.currentUser();
        if (p.getRole() == UserRole.ADMIN) {
            return;
        }
        if (p.getRole() == UserRole.CUSTOMER) {
            if (!Objects.equals(order.getCustomerId(), p.getId())) {
                throw new ResourceNotFoundException("Order not found");
            }
            return;
        }
        if (p.getRole() == UserRole.OWNER || p.getRole() == UserRole.STAFF) {
            if (p.getShopId() == null || !p.getShopId().equals(order.getShop().getId())) {
                throw new ResourceNotFoundException("Order not found");
            }
            return;
        }
        throw new ResourceNotFoundException("Order not found");
    }

    private void assertCanManage(Order order) {
        UserPrincipal p = SecurityUtils.currentUser();
        if (p.getRole() == UserRole.ADMIN) {
            return;
        }
        if ((p.getRole() == UserRole.OWNER || p.getRole() == UserRole.STAFF)
                && p.getShopId() != null
                && p.getShopId().equals(order.getShop().getId())) {
            return;
        }
        throw new ResourceNotFoundException("Order not found");
    }

    private OrderStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        return OrderStatus.valueOf(status.toUpperCase());
    }
}
