package com.acleda.bsonlineshop.service.impl;

import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.dto.review.ReviewCreateRequest;
import com.acleda.bsonlineshop.dto.review.ReviewResponse;
import com.acleda.bsonlineshop.entity.Product;
import com.acleda.bsonlineshop.entity.Review;
import com.acleda.bsonlineshop.entity.User;
import com.acleda.bsonlineshop.exception.ResourceNotFoundException;
import com.acleda.bsonlineshop.mapper.ReviewMapper;
import com.acleda.bsonlineshop.repository.ProductRepository;
import com.acleda.bsonlineshop.repository.ReviewRepository;
import com.acleda.bsonlineshop.repository.UserRepository;
import com.acleda.bsonlineshop.security.SecurityUtils;
import com.acleda.bsonlineshop.service.ReviewService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> list(UUID productId, int page, int limit) {
        return PageResponse.from(reviewRepository
                .findByProduct_IdAndDeletedFalseOrderByCreatedAtDesc(
                        productId, PageRequest.of(Math.max(page - 1, 0), limit))
                .map(reviewMapper::toResponse));
    }

    @Override
    @Transactional
    public ReviewResponse create(UUID productId, ReviewCreateRequest request) {
        Product product = productRepository.findByIdAndDeletedFalse(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        User user = userRepository.findById(SecurityUtils.currentUserId()).orElseThrow();
        Review review = new Review();
        review.setProduct(product);
        review.setCustomerId(user.getId());
        review.setCustomerName(user.getFullName());
        review.setCustomerAvatar(user.getAvatar());
        review.setCustomerAvatarAlt(user.getAvatarAlt());
        review.setRating(request.getRating());
        review.setTitle(request.getTitle());
        review.setBody(request.getBody());
        review.setSkinType(request.getSkinType() != null ? request.getSkinType() : "");
        review.setVerified(true);
        Review saved = reviewRepository.save(review);
        recalculateProductRating(product);
        return reviewMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(UUID reviewId) {
        Review review = reviewRepository.findByIdAndDeletedFalse(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        Product product = review.getProduct();
        review.setDeleted(true);
        reviewRepository.save(review);
        recalculateProductRating(product);
    }

    private void recalculateProductRating(Product product) {
        long count = reviewRepository.countByProduct_IdAndDeletedFalse(product.getId());
        double avg = reviewRepository.averageRatingByProduct(product.getId());
        product.setReviewCount((int) count);
        product.setRating(avg);
        productRepository.save(product);
    }
}
