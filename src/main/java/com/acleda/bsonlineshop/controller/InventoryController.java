package com.acleda.bsonlineshop.controller;

import com.acleda.bsonlineshop.dto.common.ApiResponse;
import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.dto.inventory.InventoryAdjustRequest;
import com.acleda.bsonlineshop.dto.inventory.InventoryCreateRequest;
import com.acleda.bsonlineshop.dto.inventory.InventoryItemResponse;
import com.acleda.bsonlineshop.dto.inventory.InventoryRestockRequest;
import com.acleda.bsonlineshop.service.InventoryService;
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
@RequestMapping("/inventory")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("@authz.adminOrMerchant()")
public class InventoryController {

    private final InventoryService inventoryService;

    @Operation(summary = "List inventory", description = "Paginated inventory lines for a shop with optional status and search.")
    @GetMapping
    public ApiResponse<PageResponse<InventoryItemResponse>> list(
            @RequestParam UUID shopId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.success(inventoryService.list(shopId, status, search, page, limit));
    }

    @Operation(summary = "Create inventory item", description = "Manually add an inventory row for a shop.")
    @PostMapping
    public ApiResponse<InventoryItemResponse> create(
            @RequestParam UUID shopId, @Valid @RequestBody InventoryCreateRequest request) {
        return ApiResponse.success("Inventory item created", inventoryService.create(shopId, request));
    }

    @Operation(summary = "Restock item", description = "Increase current stock for an inventory row and refresh LOW/HEALTHY status.")
    @PatchMapping("/{id}/restock")
    public ApiResponse<InventoryItemResponse> restock(
            @PathVariable UUID id, @Valid @RequestBody InventoryRestockRequest request) {
        return ApiResponse.success("Restocked", inventoryService.restock(id, request));
    }

    @Operation(summary = "Adjust stock", description = "Apply a positive or negative stock delta and log a stock event.")
    @PatchMapping("/{id}/adjust")
    public ApiResponse<InventoryItemResponse> adjust(
            @PathVariable UUID id, @Valid @RequestBody InventoryAdjustRequest request) {
        return ApiResponse.success("Stock adjusted", inventoryService.adjust(id, request));
    }
}
