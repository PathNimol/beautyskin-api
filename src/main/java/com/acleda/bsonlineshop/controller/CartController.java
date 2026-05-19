package com.acleda.bsonlineshop.controller;

import com.acleda.bsonlineshop.dto.cart.CartItemRequest;
import com.acleda.bsonlineshop.dto.cart.CartResponse;
import com.acleda.bsonlineshop.dto.common.ApiResponse;
import com.acleda.bsonlineshop.service.CartService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/  cart")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ApiResponse<CartResponse> get() {
        return ApiResponse.success(cartService.getCart());
    }

    @PostMapping("/items")
    public ApiResponse<CartResponse> add(@Valid @RequestBody CartItemRequest request) {
        return ApiResponse.success(cartService.addItem(request));
    }

    @PatchMapping("/items/{itemId}")
    public ApiResponse<CartResponse> update(@PathVariable UUID itemId, @RequestBody Map<String, Integer> body) {
        return ApiResponse.success(cartService.updateQuantity(itemId, body.getOrDefault("quantity", 1)));
    }

    @DeleteMapping("/items/{itemId}")
    public ApiResponse<CartResponse> remove(@PathVariable UUID itemId) {
        return ApiResponse.success(cartService.removeItem(itemId));
    }

    @DeleteMapping
    public ApiResponse<CartResponse> clear() {
        return ApiResponse.success(cartService.clear());
    }

    @PostMapping("/promo")
    public ApiResponse<CartResponse> promo(@RequestBody Map<String, String> body) {
        return ApiResponse.success(cartService.applyPromo(body.get("code")));
    }
}
