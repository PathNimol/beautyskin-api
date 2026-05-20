package com.acleda.bsonlineshop.controller;

import com.acleda.bsonlineshop.dto.common.ApiResponse;
import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.entity.Product;
import com.acleda.bsonlineshop.entity.ProductRevokeRequest;
import com.acleda.bsonlineshop.entity.Shop;
import com.acleda.bsonlineshop.enums.RevokeRequestStatus;
import com.acleda.bsonlineshop.exception.ResourceNotFoundException;
import com.acleda.bsonlineshop.repository.ProductRepository;
import com.acleda.bsonlineshop.repository.ProductRevokeRequestRepository;
import com.acleda.bsonlineshop.repository.ShopRepository;
import com.acleda.bsonlineshop.security.SecurityUtils;
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
@RequestMapping("/revoke-requests")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("@authz.adminOrMerchant()")
public class RevokeRequestController {

    private final ProductRevokeRequestRepository revokeRepository;
    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;

    @Operation(summary = "List revoke requests", description = "Paginated product revoke / return requests for a shop.")
    @GetMapping
    public ApiResponse<PageResponse<ProductRevokeRequest>> list(
            @RequestParam UUID shopId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        RevokeRequestStatus rs = status != null ? RevokeRequestStatus.valueOf(status.toUpperCase()) : null;
        var result = revokeRepository.findByShop(SecurityUtils.requireShopId(shopId), rs, PageRequest.of(Math.max(page - 1, 0), limit));
        return ApiResponse.success(PageResponse.from(result));
    }

    @Operation(summary = "Submit revoke request", description = "Request to revoke or return stock for a product (quantity, reason, detail).")
    @PostMapping
    public ApiResponse<ProductRevokeRequest> create(@RequestParam UUID shopId, @RequestBody Map<String, Object> body) {
        Shop shop = shopRepository.findByIdAndDeletedFalse(SecurityUtils.requireShopId(shopId))
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));
        Product product = productRepository.findByIdAndDeletedFalse(UUID.fromString(body.get("productId").toString()))
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        ProductRevokeRequest req = new ProductRevokeRequest();
        req.setShop(shop);
        req.setProduct(product);
        req.setSku(product.getSku());
        req.setQuantity((Integer) body.get("quantity"));
        req.setReason((String) body.get("reason"));
        req.setDetail((String) body.getOrDefault("detail", ""));
        req.setStatus(RevokeRequestStatus.PENDING);
        return ApiResponse.success("Request submitted", revokeRepository.save(req));
    }

    @Operation(summary = "Review revoke request", description = "Approve or reject a revoke request (status, notes). Sets reviewedBy from current user.")
    @PatchMapping("/{id}/review")
    public ApiResponse<ProductRevokeRequest> review(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        ProductRevokeRequest req = revokeRepository.findById(id).filter(r -> !r.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));
        req.setStatus(RevokeRequestStatus.valueOf(body.get("status").toUpperCase()));
        req.setReviewNotes(body.get("notes"));
        req.setReviewedBy(SecurityUtils.currentUser().getEmail());
        return ApiResponse.success(revokeRepository.save(req));
    }
}
