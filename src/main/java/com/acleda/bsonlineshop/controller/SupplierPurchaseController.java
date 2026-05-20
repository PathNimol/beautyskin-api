package com.acleda.bsonlineshop.controller;

import com.acleda.bsonlineshop.dto.common.ApiResponse;
import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.entity.Shop;
import com.acleda.bsonlineshop.entity.Supplier;
import com.acleda.bsonlineshop.entity.SupplierPurchase;
import com.acleda.bsonlineshop.entity.SupplierPurchaseLine;
import com.acleda.bsonlineshop.enums.SupplierPurchaseStatus;
import com.acleda.bsonlineshop.exception.ResourceNotFoundException;
import com.acleda.bsonlineshop.repository.ShopRepository;
import com.acleda.bsonlineshop.repository.SupplierPurchaseRepository;
import com.acleda.bsonlineshop.repository.SupplierRepository;
import com.acleda.bsonlineshop.security.SecurityUtils;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/supplier-purchases")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("@authz.adminOrMerchant()")
public class SupplierPurchaseController {

    private final SupplierPurchaseRepository purchaseRepository;
    private final SupplierRepository supplierRepository;
    private final ShopRepository shopRepository;

    @Operation(summary = "List purchase orders", description = "Paginated supplier purchase orders for the scoped shop.")
    @GetMapping
    public ApiResponse<PageResponse<SupplierPurchase>> list(
            @RequestParam(required = false) UUID shopId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        SupplierPurchaseStatus ps = status != null ? SupplierPurchaseStatus.valueOf(status.toUpperCase()) : null;
        return ApiResponse.success(PageResponse.from(
                purchaseRepository.search(SecurityUtils.requireShopId(shopId), ps, PageRequest.of(Math.max(page - 1, 0), limit))));
    }

    @Operation(summary = "Create purchase order", description = "Create a PO for a supplier with line items (supplierId, items with productName, sku, quantity, unitCost).")
    @PostMapping
    public ApiResponse<SupplierPurchase> create(@RequestParam UUID shopId, @RequestBody Map<String, Object> body) {
        Shop shop = shopRepository.findByIdAndDeletedFalse(SecurityUtils.requireShopId(shopId))
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));
        Supplier supplier = supplierRepository.findById(UUID.fromString(body.get("supplierId").toString()))
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));
        SupplierPurchase purchase = new SupplierPurchase();
        purchase.setPurchaseRef("PO-" + System.currentTimeMillis());
        purchase.setShop(shop);
        purchase.setSupplier(supplier);
        purchase.setOrderDate(LocalDate.now());
        purchase.setStatus(SupplierPurchaseStatus.PENDING);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> lines = (List<Map<String, Object>>) body.get("items");
        BigDecimal total = BigDecimal.ZERO;
        for (Map<String, Object> line : lines) {
            SupplierPurchaseLine pl = new SupplierPurchaseLine();
            pl.setPurchase(purchase);
            pl.setProductName((String) line.get("productName"));
            pl.setSku((String) line.get("sku"));
            pl.setQuantity((Integer) line.get("quantity"));
            pl.setUnitCost(new BigDecimal(line.get("unitCost").toString()));
            pl.setLineTotal(pl.getUnitCost().multiply(BigDecimal.valueOf(pl.getQuantity())));
            purchase.getLines().add(pl);
            total = total.add(pl.getLineTotal());
        }
        purchase.setTotal(total);
        return ApiResponse.success("Purchase order created", purchaseRepository.save(purchase));
    }

    @Operation(summary = "Update purchase status", description = "Change PO lifecycle status (body: { \"status\" }).")
    @PatchMapping("/{id}/status")
    public ApiResponse<SupplierPurchase> updateStatus(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        SupplierPurchase purchase = purchaseRepository.findById(id).filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Purchase not found"));
        purchase.setStatus(SupplierPurchaseStatus.valueOf(body.get("status").toUpperCase()));
        return ApiResponse.success(purchaseRepository.save(purchase));
    }
}
