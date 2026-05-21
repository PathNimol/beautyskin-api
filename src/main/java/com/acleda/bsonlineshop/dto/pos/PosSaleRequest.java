package com.acleda.bsonlineshop.dto.pos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class PosSaleRequest {
    private String customerName;
    private String customerPhone;
    @NotBlank
    private String paymentMethod;
    @NotEmpty
    @Valid
    private List<PosSaleLineRequest> items;
    private BigDecimal discount;
}
