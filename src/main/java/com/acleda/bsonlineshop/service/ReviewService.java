package com.acleda.bsonlineshop.service;

import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.dto.review.ReviewCreateRequest;
import com.acleda.bsonlineshop.dto.review.ReviewResponse;
import java.util.UUID;

public interface ReviewService {
    PageResponse<ReviewResponse> list(UUID productId, int page, int limit);

    ReviewResponse create(UUID productId, ReviewCreateRequest request);

    void delete(UUID reviewId);
}
