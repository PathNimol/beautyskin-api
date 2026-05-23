package com.acleda.bsonlineshop.service;

import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.dto.promotion.PromotionCreateRequest;
import com.acleda.bsonlineshop.dto.promotion.PromotionResponse;
import com.acleda.bsonlineshop.dto.promotion.PromotionUpdateRequest;
import com.acleda.bsonlineshop.enums.PromotionStatus;
import java.util.UUID;

public interface PromotionService {
    PageResponse<PromotionResponse> list(UUID shopId, String status, String search, int page, int limit);

    PromotionResponse create(UUID shopId, PromotionCreateRequest request);

    PromotionResponse update(UUID id, PromotionUpdateRequest request);

    PromotionResponse updateStatus(UUID id, PromotionStatus status);

    void delete(UUID id);
}
