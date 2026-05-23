package com.acleda.bsonlineshop.controller;

import com.acleda.bsonlineshop.dto.cart.CartResponse;
import com.acleda.bsonlineshop.dto.common.ApiResponse;
import com.acleda.bsonlineshop.service.CartService;
import java.math.BigDecimal;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/checkout")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("@authz.storeOrderActor()")
public class CheckoutController {

    private final CartService cartService;

    @Operation(summary = "Checkout quote", description = "Preview totals from the current cart: subtotal, promo discount, auto discount, shipping placeholder, and line items.")
    @GetMapping("/quote")
    public ApiResponse<Map<String, Object>> quote() {
        CartResponse cart = cartService.getCart();
        BigDecimal autoDiscount = cart.getSubtotal().multiply(BigDecimal.valueOf(0.10));
        return ApiResponse.success(Map.of(
                "subtotal", cart.getSubtotal(),
                "promoDiscount", cart.getDiscount(),
                "autoDiscount", autoDiscount,
                "shipping", BigDecimal.ZERO,
                "total", cart.getSubtotal().subtract(cart.getDiscount()).subtract(autoDiscount).max(BigDecimal.ZERO),
                "items", cart.getItems()));
    }
}
