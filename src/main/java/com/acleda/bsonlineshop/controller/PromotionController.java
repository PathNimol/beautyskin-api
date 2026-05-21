package com.acleda.bsonlineshop.controller;

import com.acleda.bsonlineshop.dto.common.ApiResponse;
import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.dto.common.StatusUpdateRequest;
import com.acleda.bsonlineshop.dto.promotion.PromotionCreateRequest;
import com.acleda.bsonlineshop.dto.promotion.PromotionResponse;
import com.acleda.bsonlineshop.dto.promotion.PromotionUpdateRequest;
import com.acleda.bsonlineshop.enums.PromotionStatus;
import com.acleda.bsonlineshop.service.PromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/promotions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("@authz.adminOrMerchant()")
public class PromotionController {

    private final PromotionService promotionService;

    @Operation(summary = "List promotions", description = "Paginated promotions for a shop with optional status filter and search.")
    @GetMapping
    public ApiResponse<PageResponse<PromotionResponse>> list(
            @RequestParam(required = false) UUID shopId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.success(promotionService.list(shopId, status, search, page, limit));
    }

    @Operation(summary = "Create promotion", description = "Create a shop promotion with typed request body.")
    @PostMapping
    public ApiResponse<PromotionResponse> create(
            @RequestParam UUID shopId, @Valid @RequestBody PromotionCreateRequest request) {
        return ApiResponse.success("Promotion created", promotionService.create(shopId, request));
    }

    @Operation(summary = "Update promotion", description = "Update promotion fields by id.")
    @PutMapping("/{id}")
    public ApiResponse<PromotionResponse> update(
            @PathVariable UUID id, @Valid @RequestBody PromotionUpdateRequest request) {
        return ApiResponse.success("Promotion updated", promotionService.update(id, request));
    }

    @Operation(summary = "Update promotion status", description = "Activate, pause, or expire a promotion by id.")
    @PatchMapping("/{id}/status")
    public ApiResponse<PromotionResponse> updateStatus(
            @PathVariable UUID id, @Valid @RequestBody StatusUpdateRequest body) {
        PromotionStatus status = PromotionStatus.valueOf(body.getStatus().toUpperCase());
        return ApiResponse.success(promotionService.updateStatus(id, status));
    }

    @Operation(summary = "Delete promotion", description = "Soft-delete a promotion.")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        promotionService.delete(id);
        return ApiResponse.success("Promotion deleted", null);
    }
}
