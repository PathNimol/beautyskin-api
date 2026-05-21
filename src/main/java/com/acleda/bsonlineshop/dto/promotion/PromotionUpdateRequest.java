package com.acleda.bsonlineshop.dto.promotion;

import com.acleda.bsonlineshop.enums.PromotionType;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class PromotionUpdateRequest {
    private String name;
    private String code;
    private PromotionType type;
    private BigDecimal value;
    private BigDecimal minOrder;
    private Integer maxUses;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
}
