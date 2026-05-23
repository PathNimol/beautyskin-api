package com.acleda.bsonlineshop.service;

import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.dto.shop.ShopNameChangeCreateRequest;
import com.acleda.bsonlineshop.dto.shop.ShopNameChangeResponse;
import com.acleda.bsonlineshop.dto.shop.ShopNameChangeReviewRequest;
import java.util.UUID;

public interface ShopNameChangeService {
    PageResponse<ShopNameChangeResponse> list(UUID shopId, String status, int page, int limit);

    ShopNameChangeResponse create(UUID shopId, ShopNameChangeCreateRequest request);

    ShopNameChangeResponse review(UUID id, ShopNameChangeReviewRequest request);
}
