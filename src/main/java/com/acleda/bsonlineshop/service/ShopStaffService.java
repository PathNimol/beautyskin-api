package com.acleda.bsonlineshop.service;

import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.dto.shopstaff.ShopStaffCreateRequest;
import com.acleda.bsonlineshop.dto.shopstaff.ShopStaffResponse;
import com.acleda.bsonlineshop.dto.shopstaff.ShopStaffUpdateRequest;
import java.util.UUID;

public interface ShopStaffService {
    PageResponse<ShopStaffResponse> list(UUID shopId, String role, String status, String search, int page, int limit);

    ShopStaffResponse create(UUID shopId, ShopStaffCreateRequest request);

    ShopStaffResponse update(UUID shopId, UUID userId, ShopStaffUpdateRequest request);

    void delete(UUID shopId, UUID userId);
}
