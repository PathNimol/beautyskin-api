package com.acleda.bsonlineshop.controller;

import com.acleda.bsonlineshop.dto.common.ApiResponse;
import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.entity.Product;
import com.acleda.bsonlineshop.entity.Review;
import com.acleda.bsonlineshop.entity.User;
import com.acleda.bsonlineshop.exception.ResourceNotFoundException;
import com.acleda.bsonlineshop.repository.ProductRepository;
import com.acleda.bsonlineshop.repository.ReviewRepository;
import com.acleda.bsonlineshop.repository.UserRepository;
import com.acleda.bsonlineshop.security.SecurityUtils;
import java.util.Map;
import java.util.UUID;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ApiResponse<PageResponse<Review>> list(
            @PathVariable UUID productId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.success(PageResponse.from(
                reviewRepository.findByProduct_IdAndDeletedFalseOrderByCreatedAtDesc(
                        productId, PageRequest.of(Math.max(page - 1, 0), limit))));
    }

    @PostMapping
    public ApiResponse<Review> create(@PathVariable UUID productId, @RequestBody Map<String, Object> body) {
        Product product = productRepository.findByIdAndDeletedFalse(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        User user = userRepository.findById(SecurityUtils.currentUserId()).orElseThrow();
        Review review = new Review();
        review.setProduct(product);
        review.setCustomerId(user.getId());
        review.setCustomerName(user.getFullName());
        review.setCustomerAvatar(user.getAvatar());
        review.setCustomerAvatarAlt(user.getAvatarAlt());
        review.setRating((Integer) body.get("rating"));
        review.setTitle((String) body.get("title"));
        review.setBody((String) body.get("body"));
        review.setSkinType((String) body.getOrDefault("skinType", ""));
        review.setVerified(true);
        Review saved = reviewRepository.save(review);
        product.setReviewCount(product.getReviewCount() + 1);
        productRepository.save(product);
        return ApiResponse.success("Review submitted", saved);
    }
}
