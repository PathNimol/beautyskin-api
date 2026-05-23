package com.acleda.bsonlineshop.controller;

import com.acleda.bsonlineshop.dto.common.ApiResponse;
import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.dto.shopstaff.ShopStaffCreateRequest;
import com.acleda.bsonlineshop.dto.shopstaff.ShopStaffResponse;
import com.acleda.bsonlineshop.dto.shopstaff.ShopStaffUpdateRequest;
import com.acleda.bsonlineshop.service.ShopStaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/shops/{shopId}/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("@authz.admin() || @authz.merchantInShop(#shopId)")
public class ShopUserController {

    private final ShopStaffService shopStaffService;

    @Operation(summary = "List shop staff", description = "Paginated staff for a shop with optional role, status, and search filters.")
    @GetMapping
    public ApiResponse<PageResponse<ShopStaffResponse>> list(
            @PathVariable UUID shopId,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.success(shopStaffService.list(shopId, role, status, search, page, limit));
    }

    @Operation(summary = "Add shop staff", description = "Create a staff record linked to the shop.")
    @PostMapping
    public ApiResponse<ShopStaffResponse> create(
            @PathVariable UUID shopId, @Valid @RequestBody ShopStaffCreateRequest request) {
        return ApiResponse.success("User added", shopStaffService.create(shopId, request));
    }

    @Operation(summary = "Update shop staff", description = "Patch staff fields for a user in the shop.")
    @PutMapping("/{userId}")
    public ApiResponse<ShopStaffResponse> update(
            @PathVariable UUID shopId,
            @PathVariable UUID userId,
            @Valid @RequestBody ShopStaffUpdateRequest request) {
        return ApiResponse.success(shopStaffService.update(shopId, userId, request));
    }

    @Operation(summary = "Remove shop staff", description = "Soft-delete a staff member from the shop.")
    @DeleteMapping("/{userId}")
    public ApiResponse<Void> delete(@PathVariable UUID shopId, @PathVariable UUID userId) {
        shopStaffService.delete(shopId, userId);
        return ApiResponse.success("User removed", null);
    }
}
