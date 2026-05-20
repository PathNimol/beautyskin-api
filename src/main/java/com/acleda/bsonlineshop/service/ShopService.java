package com.acleda.bsonlineshop.service;

import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.dto.shop.ShopCreateRequest;
import com.acleda.bsonlineshop.dto.shop.ShopResponse;
import com.acleda.bsonlineshop.enums.ShopStatus;
import java.util.UUID;

public interface ShopService {
    PageResponse<ShopResponse> list(String status, String search, int page, int limit);
    ShopResponse getById(UUID id);
    ShopResponse create(ShopCreateRequest request);
    ShopResponse updateStatus(UUID id, ShopStatus status);
}
