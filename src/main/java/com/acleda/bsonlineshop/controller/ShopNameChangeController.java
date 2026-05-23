package com.acleda.bsonlineshop.controller;

import com.acleda.bsonlineshop.dto.common.ApiResponse;
import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.dto.shop.ShopNameChangeCreateRequest;
import com.acleda.bsonlineshop.dto.shop.ShopNameChangeResponse;
import com.acleda.bsonlineshop.dto.shop.ShopNameChangeReviewRequest;
import com.acleda.bsonlineshop.service.ShopNameChangeService;
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
@RequestMapping("/shop-name-change-requests")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ShopNameChangeController {

    private final ShopNameChangeService shopNameChangeService;

    @Operation(summary = "List shop name change requests")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','OWNER')")
    public ApiResponse<PageResponse<ShopNameChangeResponse>> list(
            @RequestParam(required = false) UUID shopId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.success(shopNameChangeService.list(shopId, status, page, limit));
    }

    @Operation(summary = "Request a shop name change (owner)")
    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<ShopNameChangeResponse> create(
            @RequestParam UUID shopId, @Valid @RequestBody ShopNameChangeCreateRequest request) {
        return ApiResponse.success("Name change request submitted", shopNameChangeService.create(shopId, request));
    }

    @Operation(summary = "Approve or reject a shop name change (admin)")
    @PatchMapping("/{id}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ShopNameChangeResponse> review(
            @PathVariable UUID id, @Valid @RequestBody ShopNameChangeReviewRequest request) {
        return ApiResponse.success(shopNameChangeService.review(id, request));
    }
}
