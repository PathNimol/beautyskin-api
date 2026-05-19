package com.acleda.bsonlineshop.dto.cart;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartResponse {
    private List<CartItemResponse> items;
    private int itemCount;
    private BigDecimal subtotal;
    private String appliedPromoCode;
    private BigDecimal discount;
    private BigDecimal total;
}
