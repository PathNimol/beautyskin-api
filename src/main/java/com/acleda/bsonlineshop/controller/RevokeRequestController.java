package com.acleda.bsonlineshop.controller;

import com.acleda.bsonlineshop.dto.common.ApiResponse;
import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.dto.revoke.RevokeCreateRequest;
import com.acleda.bsonlineshop.dto.revoke.RevokeRequestResponse;
import com.acleda.bsonlineshop.dto.revoke.RevokeReviewRequest;
import com.acleda.bsonlineshop.service.RevokeRequestService;
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
@RequestMapping("/revoke-requests")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("@authz.adminOrMerchant()")
public class RevokeRequestController {

    private final RevokeRequestService revokeRequestService;

    @Operation(summary = "List revoke requests", description = "Paginated product revoke / return requests for a shop.")
    @GetMapping
    public ApiResponse<PageResponse<RevokeRequestResponse>> list(
            @RequestParam UUID shopId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.success(revokeRequestService.list(shopId, status, page, limit));
    }

    @Operation(summary = "Submit revoke request", description = "Request to revoke or return stock for a product.")
    @PostMapping
    public ApiResponse<RevokeRequestResponse> create(
            @RequestParam UUID shopId, @Valid @RequestBody RevokeCreateRequest request) {
        return ApiResponse.success("Request submitted", revokeRequestService.create(shopId, request));
    }

    @Operation(summary = "Review revoke request", description = "Approve or reject a revoke request.")
    @PatchMapping("/{id}/review")
    public ApiResponse<RevokeRequestResponse> review(
            @PathVariable UUID id, @Valid @RequestBody RevokeReviewRequest request) {
        return ApiResponse.success(revokeRequestService.review(id, request));
    }
}
