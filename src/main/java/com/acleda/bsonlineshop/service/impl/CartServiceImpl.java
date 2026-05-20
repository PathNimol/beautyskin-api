package com.acleda.bsonlineshop.service.impl;

import com.acleda.bsonlineshop.dto.cart.CartItemRequest;
import com.acleda.bsonlineshop.dto.cart.CartItemResponse;
import com.acleda.bsonlineshop.dto.cart.CartResponse;
import com.acleda.bsonlineshop.entity.Cart;
import com.acleda.bsonlineshop.entity.CartItem;
import com.acleda.bsonlineshop.entity.Product;
import com.acleda.bsonlineshop.entity.Promotion;
import com.acleda.bsonlineshop.entity.User;
import com.acleda.bsonlineshop.enums.PromotionStatus;
import com.acleda.bsonlineshop.enums.PromotionType;
import com.acleda.bsonlineshop.exception.BusinessException;
import com.acleda.bsonlineshop.exception.ResourceNotFoundException;
import com.acleda.bsonlineshop.repository.CartRepository;
import com.acleda.bsonlineshop.repository.ProductRepository;
import com.acleda.bsonlineshop.repository.PromotionRepository;
import com.acleda.bsonlineshop.repository.UserRepository;
import com.acleda.bsonlineshop.security.SecurityUtils;
import com.acleda.bsonlineshop.service.CartService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PromotionRepository promotionRepository;

    @Override
    @Transactional  // ← remove readOnly=true so cart creation works
    public CartResponse getCart() {
        return toResponse(getOrCreateCart());
    }

    @Override
    @Transactional
    public CartResponse addItem(CartItemRequest request) {
        Cart cart = getOrCreateCart();
        Product product = productRepository.findByIdAndDeletedFalse(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        CartItem existing = cart.getItems().stream()
                .filter(i -> product.getId().equals(i.getProductId()))
                .findFirst()
                .orElse(null);
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + request.getQuantity());
        } else {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProductId(product.getId());
            item.setName(product.getName());
            item.setBrand(product.getBrand());
            item.setPrice(product.getPrice());
            item.setQuantity(request.getQuantity());
            item.setImage(product.getImage());
            item.setImageAlt(product.getImageAlt());
            item.setShopId(product.getShop().getId());
            item.setShopName(product.getShop().getName());
            cart.getItems().add(item);
        }
        return toResponse(cartRepository.save(cart));
    }

    @Override
    @Transactional
    public CartResponse updateQuantity(UUID itemId, int quantity) {
        Cart cart = getOrCreateCart();
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        if (quantity <= 0) {
            cart.getItems().remove(item);
        } else {
            item.setQuantity(quantity);
        }
        return toResponse(cartRepository.save(cart));
    }

    @Override
    @Transactional
    public CartResponse removeItem(UUID itemId) {
        Cart cart = getOrCreateCart();
        cart.getItems().removeIf(i -> i.getId().equals(itemId));
        return toResponse(cartRepository.save(cart));
    }

    @Override
    @Transactional
    public CartResponse clear() {
        Cart cart = getOrCreateCart();
        cart.getItems().clear();
        cart.setAppliedPromoCode(null);
        return toResponse(cartRepository.save(cart));
    }

    @Override
    @Transactional
    public CartResponse applyPromo(String code) {
        Cart cart = getOrCreateCart();
        Promotion promo = promotionRepository.findByCodeIgnoreCaseAndDeletedFalse(code)
                .orElseThrow(() -> new BusinessException("Invalid promo code"));
        if (promo.getStatus() != PromotionStatus.ACTIVE) {
            throw new BusinessException("Promotion is not active");
        }
        cart.setAppliedPromoCode(code);
        return toResponse(cartRepository.save(cart));
    }

    private Cart getOrCreateCart() {
        User user = userRepository.findById(SecurityUtils.currentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return cartRepository.findByUserAndDeletedFalse(user).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setUser(user);
            return cartRepository.save(cart);
        });
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream().map(i -> CartItemResponse.builder()
                .id(i.getId())
                .productId(i.getProductId())
                .name(i.getName())
                .brand(i.getBrand())
                .price(i.getPrice())
                .quantity(i.getQuantity())
                .image(i.getImage())
                .imageAlt(i.getImageAlt())
                .shopId(i.getShopId())
                .shopName(i.getShopName())
                .lineTotal(i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .build()).toList();
        BigDecimal subtotal = items.stream()
                .map(CartItemResponse::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discount = calculateDiscount(cart, subtotal);
        return CartResponse.builder()
                .items(items)
                .itemCount(items.stream().mapToInt(CartItemResponse::getQuantity).sum())
                .subtotal(subtotal)
                .appliedPromoCode(cart.getAppliedPromoCode())
                .discount(discount)
                .total(subtotal.subtract(discount).max(BigDecimal.ZERO))
                .build();
    }

    private BigDecimal calculateDiscount(Cart cart, BigDecimal subtotal) {
        if (cart.getAppliedPromoCode() == null) {
            return BigDecimal.ZERO;
        }
        return promotionRepository.findByCodeIgnoreCaseAndDeletedFalse(cart.getAppliedPromoCode())
                .map(p -> {
                    if (p.getMinOrder() != null && subtotal.compareTo(p.getMinOrder()) < 0) {
                        return BigDecimal.ZERO;
                    }
                    if (p.getType() == PromotionType.PERCENTAGE) {
                        return subtotal.multiply(p.getValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    }
                    if (p.getType() == PromotionType.FIXED) {
                        return p.getValue().min(subtotal);
                    }
                    return BigDecimal.ZERO;
                })
                .orElse(BigDecimal.ZERO);
    }
}
