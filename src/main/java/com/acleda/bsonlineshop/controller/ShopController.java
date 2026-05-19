package com.acleda.bsonlineshop.controller;

import com.acleda.bsonlineshop.dto.common.ApiResponse;
import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.dto.shop.ShopCreateRequest;
import com.acleda.bsonlineshop.dto.shop.ShopResponse;
import com.acleda.bsonlineshop.enums.ShopStatus;
import com.acleda.bsonlineshop.service.ShopService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.Map;
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
@RequestMapping("/shops")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ShopController {

    private final ShopService shopService;

    @GetMapping
    public ApiResponse<PageResponse<ShopResponse>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.success(shopService.list(status, search, page, limit));
    }

    @GetMapping("/{id}")
    public ApiResponse<ShopResponse> get(@PathVariable UUID id) {
        return ApiResponse.success(shopService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','OWNER')")
    public ApiResponse<ShopResponse> create(@Valid @RequestBody ShopCreateRequest request) {
        return ApiResponse.success("Shop created", shopService.create(request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ShopResponse> updateStatus(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        ShopStatus status = ShopStatus.valueOf(body.get("status").toUpperCase());
        return ApiResponse.success(shopService.updateStatus(id, status));
    }
}
