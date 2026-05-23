package com.acleda.bsonlineshop.controller;

import com.acleda.bsonlineshop.dto.common.ApiResponse;
import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.dto.review.ReviewCreateRequest;
import com.acleda.bsonlineshop.dto.review.ReviewResponse;
import com.acleda.bsonlineshop.service.ReviewService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products/{productId}/reviews")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "List product reviews", description = "Paginated reviews for a product, newest first.")
    @GetMapping
    public ApiResponse<PageResponse<ReviewResponse>> list(
            @PathVariable UUID productId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.success(reviewService.list(productId, page, limit));
    }

    @Operation(summary = "Submit review", description = "Create a customer review; recalculates product average rating.")
    @PostMapping
    @PreAuthorize("@authz.storeOrderActor()")
    public ApiResponse<ReviewResponse> create(
            @PathVariable UUID productId, @Valid @RequestBody ReviewCreateRequest request) {
        return ApiResponse.success("Review submitted", reviewService.create(productId, request));
    }

    @Operation(summary = "Delete review", description = "Soft-delete a review (admin) and recalculate product rating.")
    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable UUID productId, @PathVariable UUID reviewId) {
        reviewService.delete(reviewId);
        return ApiResponse.success("Review deleted", null);
    }
}
