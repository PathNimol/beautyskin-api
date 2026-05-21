package com.acleda.bsonlineshop.controller;

import com.acleda.bsonlineshop.dto.common.ApiResponse;
import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.dto.common.StatusUpdateRequest;
import com.acleda.bsonlineshop.dto.supplierpurchase.SupplierPurchaseCreateRequest;
import com.acleda.bsonlineshop.dto.supplierpurchase.SupplierPurchaseResponse;
import com.acleda.bsonlineshop.enums.SupplierPurchaseStatus;
import com.acleda.bsonlineshop.service.SupplierPurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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

    private final SupplierPurchaseService supplierPurchaseService;

    @Operation(summary = "List purchase orders", description = "Paginated supplier purchase orders for the scoped shop.")
    @GetMapping
    public ApiResponse<PageResponse<SupplierPurchaseResponse>> list(
            @RequestParam(required = false) UUID shopId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.success(supplierPurchaseService.list(shopId, status, page, limit));
    }

    @Operation(summary = "Create purchase order", description = "Create a PO for a supplier with line items.")
    @PostMapping
    public ApiResponse<SupplierPurchaseResponse> create(
            @RequestParam UUID shopId, @Valid @RequestBody SupplierPurchaseCreateRequest request) {
        return ApiResponse.success("Purchase order created", supplierPurchaseService.create(shopId, request));
    }

    @Operation(summary = "Update purchase status", description = "Change PO lifecycle status; RECEIVED updates stock.")
    @PatchMapping("/{id}/status")
    public ApiResponse<SupplierPurchaseResponse> updateStatus(
            @PathVariable UUID id, @Valid @RequestBody StatusUpdateRequest body) {
        SupplierPurchaseStatus status = SupplierPurchaseStatus.valueOf(body.getStatus().toUpperCase());
        return ApiResponse.success(supplierPurchaseService.updateStatus(id, status));
    }
}
