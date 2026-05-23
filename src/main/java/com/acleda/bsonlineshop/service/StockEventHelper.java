package com.acleda.bsonlineshop.service;

import com.acleda.bsonlineshop.entity.Product;
import com.acleda.bsonlineshop.entity.ProductStockEvent;
import com.acleda.bsonlineshop.repository.ProductStockEventRepository;
import com.acleda.bsonlineshop.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockEventHelper {

    private final ProductStockEventRepository stockEventRepository;

    public void record(Product product, int quantityDelta, String notes) {
        if (product == null || quantityDelta == 0) {
            return;
        }
        ProductStockEvent event = new ProductStockEvent();
        event.setProduct(product);
        event.setQuantityReceived(quantityDelta);
        event.setSupplierName("");
        event.setRecordedByRole(SecurityUtils.currentRole().name());
        event.setNotes(notes != null ? notes : "");
        stockEventRepository.save(event);
    }
}
