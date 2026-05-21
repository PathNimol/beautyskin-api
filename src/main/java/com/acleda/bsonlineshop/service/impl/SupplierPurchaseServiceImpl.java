package com.acleda.bsonlineshop.service.impl;

import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.dto.supplierpurchase.SupplierPurchaseCreateRequest;
import com.acleda.bsonlineshop.dto.supplierpurchase.SupplierPurchaseLineRequest;
import com.acleda.bsonlineshop.dto.supplierpurchase.SupplierPurchaseResponse;
import com.acleda.bsonlineshop.entity.InventoryItem;
import com.acleda.bsonlineshop.entity.Product;
import com.acleda.bsonlineshop.entity.Shop;
import com.acleda.bsonlineshop.entity.Supplier;
import com.acleda.bsonlineshop.entity.SupplierPurchase;
import com.acleda.bsonlineshop.entity.SupplierPurchaseLine;
import com.acleda.bsonlineshop.enums.SupplierPurchaseStatus;
import com.acleda.bsonlineshop.exception.ResourceNotFoundException;
import com.acleda.bsonlineshop.mapper.SupplierPurchaseMapper;
import com.acleda.bsonlineshop.repository.InventoryItemRepository;
import com.acleda.bsonlineshop.repository.ProductRepository;
import com.acleda.bsonlineshop.repository.ShopRepository;
import com.acleda.bsonlineshop.repository.SupplierPurchaseRepository;
import com.acleda.bsonlineshop.repository.SupplierRepository;
import com.acleda.bsonlineshop.security.SecurityUtils;
import com.acleda.bsonlineshop.service.StockEventHelper;
import com.acleda.bsonlineshop.service.SupplierPurchaseService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SupplierPurchaseServiceImpl implements SupplierPurchaseService {

    private final SupplierPurchaseRepository purchaseRepository;
    private final SupplierRepository supplierRepository;
    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final SupplierPurchaseMapper purchaseMapper;
    private final StockEventHelper stockEventHelper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SupplierPurchaseResponse> list(UUID shopId, String status, int page, int limit) {
        SupplierPurchaseStatus ps = status != null ? SupplierPurchaseStatus.valueOf(status.toUpperCase()) : null;
        return PageResponse.from(purchaseRepository
                .search(SecurityUtils.requireShopId(shopId), ps, PageRequest.of(Math.max(page - 1, 0), limit))
                .map(purchaseMapper::toResponse));
    }

    @Override
    @Transactional
    public SupplierPurchaseResponse create(UUID shopId, SupplierPurchaseCreateRequest request) {
        Shop shop = shopRepository.findByIdAndDeletedFalse(SecurityUtils.requireShopId(shopId))
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));
        SupplierPurchase purchase = new SupplierPurchase();
        purchase.setPurchaseRef("PO-" + System.currentTimeMillis());
        purchase.setShop(shop);
        purchase.setSupplier(supplier);
        purchase.setOrderDate(LocalDate.now());
        purchase.setExpectedDate(request.getExpectedDate());
        purchase.setStatus(SupplierPurchaseStatus.PENDING);
        BigDecimal total = BigDecimal.ZERO;
        for (SupplierPurchaseLineRequest line : request.getItems()) {
            SupplierPurchaseLine pl = new SupplierPurchaseLine();
            pl.setPurchase(purchase);
            pl.setProductId(line.getProductId());
            pl.setProductName(line.getProductName());
            pl.setSku(line.getSku());
            pl.setQuantity(line.getQuantity());
            pl.setUnitCost(line.getUnitCost());
            pl.setLineTotal(line.getUnitCost().multiply(BigDecimal.valueOf(line.getQuantity())));
            purchase.getLines().add(pl);
            total = total.add(pl.getLineTotal());
        }
        purchase.setTotal(total);
        return purchaseMapper.toResponse(purchaseRepository.save(purchase));
    }

    @Override
    @Transactional
    public SupplierPurchaseResponse updateStatus(UUID id, SupplierPurchaseStatus status) {
        SupplierPurchase purchase = purchaseRepository.findById(id).filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Purchase not found"));
        SupplierPurchaseStatus previous = purchase.getStatus();
        purchase.setStatus(status);
        if (status == SupplierPurchaseStatus.RECEIVED && previous != SupplierPurchaseStatus.RECEIVED) {
            receiveGoods(purchase);
        }
        return purchaseMapper.toResponse(purchaseRepository.save(purchase));
    }

    private void receiveGoods(SupplierPurchase purchase) {
        UUID shopId = purchase.getShop() != null ? purchase.getShop().getId() : null;
        for (SupplierPurchaseLine line : purchase.getLines()) {
            if (line.getProductId() != null) {
                productRepository.findByIdAndDeletedFalse(line.getProductId()).ifPresent(product -> {
                    product.setStock(product.getStock() + line.getQuantity());
                    productRepository.save(product);
                    stockEventHelper.record(product, line.getQuantity(), "supplier_po_received");
                });
            }
            if (shopId != null) {
                inventoryItemRepository.search(shopId, null, line.getSku(), PageRequest.of(0, 1))
                        .stream()
                        .findFirst()
                        .ifPresent(inv -> {
                            inv.setCurrentStock(inv.getCurrentStock() + line.getQuantity());
                            inv.setLastRestocked(LocalDate.now());
                            inventoryItemRepository.save(inv);
                        });
            }
        }
    }
}
