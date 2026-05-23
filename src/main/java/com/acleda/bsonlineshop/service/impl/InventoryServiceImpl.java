package com.acleda.bsonlineshop.service.impl;

import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.dto.inventory.InventoryAdjustRequest;
import com.acleda.bsonlineshop.dto.inventory.InventoryCreateRequest;
import com.acleda.bsonlineshop.dto.inventory.InventoryItemResponse;
import com.acleda.bsonlineshop.dto.inventory.InventoryRestockRequest;
import com.acleda.bsonlineshop.entity.InventoryItem;
import com.acleda.bsonlineshop.entity.Product;
import com.acleda.bsonlineshop.entity.Shop;
import com.acleda.bsonlineshop.enums.InventoryStatus;
import com.acleda.bsonlineshop.exception.ResourceNotFoundException;
import com.acleda.bsonlineshop.mapper.InventoryMapper;
import com.acleda.bsonlineshop.repository.InventoryItemRepository;
import com.acleda.bsonlineshop.repository.ProductRepository;
import com.acleda.bsonlineshop.repository.ShopRepository;
import com.acleda.bsonlineshop.enums.UserRole;
import com.acleda.bsonlineshop.security.SecurityUtils;
import com.acleda.bsonlineshop.service.InventoryService;
import com.acleda.bsonlineshop.service.StockEventHelper;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryItemRepository inventoryRepository;
    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;
    private final InventoryMapper inventoryMapper;
    private final StockEventHelper stockEventHelper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InventoryItemResponse> list(UUID shopId, String status, String search, int page, int limit) {
        InventoryStatus invStatus = parseStatus(status);
        PageRequest pageable = PageRequest.of(Math.max(page - 1, 0), limit);
        if (SecurityUtils.currentRole() == UserRole.ADMIN && shopId == null) {
            var result = inventoryRepository.searchPlatform(null, invStatus, search, pageable);
            return PageResponse.from(result.map(inventoryMapper::toResponse));
        }
        UUID scoped = SecurityUtils.requireShopId(shopId);
        var result = inventoryRepository.search(scoped, invStatus, search, pageable);
        return PageResponse.from(result.map(inventoryMapper::toResponse));
    }

    @Override
    @Transactional
    public InventoryItemResponse create(UUID shopId, InventoryCreateRequest request) {
        Shop shop = shopRepository.findByIdAndDeletedFalse(SecurityUtils.requireShopId(shopId))
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));
        InventoryItem item = new InventoryItem();
        item.setShop(shop);
        item.setProductId(request.getProductId());
        item.setProductName(request.getProductName());
        item.setSku(request.getSku());
        item.setCurrentStock(request.getCurrentStock());
        item.setMinStock(request.getMinStock() != null ? request.getMinStock() : 0);
        item.setMaxStock(request.getMaxStock() != null ? request.getMaxStock() : 9999);
        item.setReorderPoint(request.getReorderPoint() != null ? request.getReorderPoint() : 10);
        item.setExpiryDate(request.getExpiryDate());
        item.setBatchNumber(request.getBatchNumber());
        item.setSupplierId(request.getSupplierId());
        item.setSupplierName(request.getSupplierName());
        item.setCostPrice(request.getCostPrice());
        item.setLastRestocked(LocalDate.now());
        refreshStatus(item);
        InventoryItem saved = inventoryRepository.save(item);
        syncProductStock(request.getProductId(), request.getCurrentStock(), "inventory_create");
        return inventoryMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public InventoryItemResponse restock(UUID id, InventoryRestockRequest request) {
        InventoryItem item = load(id);
        int qty = request.getQuantity();
        item.setCurrentStock(item.getCurrentStock() + qty);
        item.setLastRestocked(LocalDate.now());
        refreshStatus(item);
        InventoryItem saved = inventoryRepository.save(item);
        syncProductStock(item.getProductId(), item.getCurrentStock(), "restock");
        productRepository.findByIdAndDeletedFalse(item.getProductId()).ifPresent(p -> stockEventHelper.record(p, qty, "restock"));
        return inventoryMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public InventoryItemResponse adjust(UUID id, InventoryAdjustRequest request) {
        InventoryItem item = load(id);
        int delta = request.getDelta();
        int newStock = Math.max(0, item.getCurrentStock() + delta);
        item.setCurrentStock(newStock);
        refreshStatus(item);
        InventoryItem saved = inventoryRepository.save(item);
        syncProductStock(item.getProductId(), newStock, "adjust");
        productRepository.findByIdAndDeletedFalse(item.getProductId()).ifPresent(p -> stockEventHelper.record(p, delta, request.getNotes()));
        return inventoryMapper.toResponse(saved);
    }

    private InventoryItem load(UUID id) {
        return inventoryRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found"));
    }

    private void refreshStatus(InventoryItem item) {
        if (item.getCurrentStock() <= 0) {
            item.setInvStatus(InventoryStatus.OUT_OF_STOCK);
        } else if (item.getCurrentStock() <= item.getReorderPoint()) {
            item.setInvStatus(InventoryStatus.LOW);
        } else {
            item.setInvStatus(InventoryStatus.HEALTHY);
        }
    }

    private void syncProductStock(UUID productId, int stock, String reason) {
        if (productId == null) return;
        productRepository.findByIdAndDeletedFalse(productId).ifPresent(p -> {
            p.setStock(stock);
            productRepository.save(p);
        });
    }

    private static InventoryStatus parseStatus(String status) {
        if (status == null || status.isBlank()) return null;
        return InventoryStatus.valueOf(status.toUpperCase().replace(' ', '_'));
    }
}
