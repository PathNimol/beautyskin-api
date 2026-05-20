package com.acleda.bsonlineshop.service;

import com.acleda.bsonlineshop.dto.cart.CartItemRequest;
import com.acleda.bsonlineshop.dto.cart.CartResponse;
import java.util.UUID;

public interface CartService {
    CartResponse getCart();
    CartResponse addItem(CartItemRequest request);
    CartResponse updateQuantity(UUID itemId, int quantity);
    CartResponse removeItem(UUID itemId);
    CartResponse clear();
    CartResponse applyPromo(String code);
}
