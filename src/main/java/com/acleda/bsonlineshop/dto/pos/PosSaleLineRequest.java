package com.acleda.bsonlineshop.dto.pos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Data;

@Data
public class PosSaleLineRequest {
    private UUID productId;
    @NotBlank
    private String name;
    @NotNull
    @Min(1)
    private Integer quantity;
    @NotNull
    private BigDecimal price;
}
