package com.acleda.bsonlineshop.dto.promotion;

import com.acleda.bsonlineshop.enums.PromotionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class PromotionCreateRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String code;
    @NotNull
    private PromotionType type;
    @NotNull
    private BigDecimal value;
    private BigDecimal minOrder;
    private Integer maxUses;
    @NotNull
    private LocalDate startDate;
    @NotNull
    private LocalDate endDate;
    private String description;
}
