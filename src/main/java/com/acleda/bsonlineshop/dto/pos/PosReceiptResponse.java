package com.acleda.bsonlineshop.dto.pos;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PosReceiptResponse {
    private UUID id;
    private String receiptRef;
    private UUID shopId;
    private String customerName;
    private String customerPhone;
    private String paymentMethod;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal tax;
    private BigDecimal total;
    private boolean cancelled;
    private List<PosReceiptLineResponse> lines;
    private Instant createdAt;
}
