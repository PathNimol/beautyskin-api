package com.acleda.bsonlineshop.controller;

import com.acleda.bsonlineshop.dto.cart.CartItemRequest;
import com.acleda.bsonlineshop.dto.cart.CartResponse;
import com.acleda.bsonlineshop.dto.common.ApiResponse;
import com.acleda.bsonlineshop.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("@authz.storeOrderActor()")
public class CartController {

    private final CartService cartService;

    @Operation(summary = "Get cart", description = "Return the current user's cart with line items, subtotal, and applied discounts.")
    @GetMapping
    public ApiResponse<CartResponse> get() {
        return ApiResponse.success(cartService.getCart());
    }

    @Operation(summary = "Add cart item", description = "Add a product line to the cart or merge quantity if already present.")
    @PostMapping("/items")
    public ApiResponse<CartResponse> add(@Valid @RequestBody CartItemRequest request) {
        return ApiResponse.success(cartService.addItem(request));
    }

    @Operation(summary = "Update cart line quantity", description = "Set quantity for a cart line item by itemId.")
    @PatchMapping("/items/{itemId}")
    public ApiResponse<CartResponse> update(@PathVariable UUID itemId, @RequestBody Map<String, Integer> body) {
        return ApiResponse.success(cartService.updateQuantity(itemId, body.getOrDefault("quantity", 1)));
    }

    @Operation(summary = "Remove cart line", description = "Remove one line item from the cart.")
    @DeleteMapping("/items/{itemId}")
    public ApiResponse<CartResponse> remove(@PathVariable UUID itemId) {
        return ApiResponse.success(cartService.removeItem(itemId));
    }

    @Operation(summary = "Clear cart", description = "Remove all items from the current user's cart.")
    @DeleteMapping
    public ApiResponse<CartResponse> clear() {
        return ApiResponse.success(cartService.clear());
    }

    @Operation(summary = "Apply promo code", description = "Validate and apply a promotion code to the cart (body: { \"code\" }).")
    @PostMapping("/promo")
    public ApiResponse<CartResponse> promo(@RequestBody Map<String, String> body) {
        return ApiResponse.success(cartService.applyPromo(body.get("code")));
    }

    @Operation(summary = "Remove promo code", description = "Clear the applied promotion from the cart without removing items.")
    @DeleteMapping("/promo")
    public ApiResponse<CartResponse> removePromo() {
        return ApiResponse.success(cartService.removePromo());
    }
}
