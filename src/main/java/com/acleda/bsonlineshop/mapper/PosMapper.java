package com.acleda.bsonlineshop.mapper;

import com.acleda.bsonlineshop.dto.pos.PosReceiptLineResponse;
import com.acleda.bsonlineshop.dto.pos.PosReceiptResponse;
import com.acleda.bsonlineshop.entity.PosReceipt;
import com.acleda.bsonlineshop.entity.PosReceiptLine;
import org.springframework.stereotype.Component;

@Component
public class PosMapper {

    public PosReceiptResponse toResponse(PosReceipt r) {
        return PosReceiptResponse.builder()
                .id(r.getId())
                .receiptRef(r.getReceiptRef())
                .shopId(r.getShop() != null ? r.getShop().getId() : null)
                .customerName(r.getCustomerName())
                .customerPhone(r.getCustomerPhone())
                .paymentMethod(r.getPaymentMethod())
                .subtotal(r.getSubtotal())
                .discount(r.getDiscount())
                .tax(r.getTax())
                .total(r.getTotal())
                .cancelled(r.isCancelled())
                .lines(r.getLines().stream().map(this::toLine).toList())
                .createdAt(r.getCreatedAt())
                .build();
    }

    private PosReceiptLineResponse toLine(PosReceiptLine line) {
        return PosReceiptLineResponse.builder()
                .productId(line.getProductId())
                .productName(line.getProductName())
                .quantity(line.getQuantity())
                .unitPrice(line.getUnitPrice())
                .lineTotal(line.getLineTotal())
                .build();
    }
}
