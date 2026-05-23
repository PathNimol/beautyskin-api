package com.acleda.bsonlineshop.service;

import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.dto.inventory.InventoryAdjustRequest;
import com.acleda.bsonlineshop.dto.inventory.InventoryCreateRequest;
import com.acleda.bsonlineshop.dto.inventory.InventoryItemResponse;
import com.acleda.bsonlineshop.dto.inventory.InventoryRestockRequest;
import java.util.UUID;

public interface InventoryService {
    PageResponse<InventoryItemResponse> list(UUID shopId, String status, String search, int page, int limit);

    InventoryItemResponse create(UUID shopId, InventoryCreateRequest request);

    InventoryItemResponse restock(UUID id, InventoryRestockRequest request);

    InventoryItemResponse adjust(UUID id, InventoryAdjustRequest request);
}
