package com.acleda.bsonlineshop.dto.order;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlaceOrderResult {

    /** One row per shop in the cart; amounts use the same promo split logic as checkout. */
    private List<OrderResponse> orders;
}
