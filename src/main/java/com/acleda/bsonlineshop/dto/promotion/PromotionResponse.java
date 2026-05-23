package com.acleda.bsonlineshop.dto.promotion;

import com.acleda.bsonlineshop.enums.PromotionStatus;
import com.acleda.bsonlineshop.enums.PromotionType;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PromotionResponse {
    private UUID id;
    private String name;
    private String code;
    private PromotionType type;
    private BigDecimal value;
    private BigDecimal minOrder;
    private int maxUses;
    private int usedCount;
    private LocalDate startDate;
    private LocalDate endDate;
    private PromotionStatus status;
    private UUID shopId;
    private String description;
    private Instant createdAt;
}
