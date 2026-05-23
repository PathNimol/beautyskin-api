package com.acleda.bsonlineshop.service;

import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.dto.supplierpurchase.SupplierPurchaseCreateRequest;
import com.acleda.bsonlineshop.dto.supplierpurchase.SupplierPurchaseResponse;
import com.acleda.bsonlineshop.enums.SupplierPurchaseStatus;
import java.util.UUID;

public interface SupplierPurchaseService {
    PageResponse<SupplierPurchaseResponse> list(UUID shopId, String status, int page, int limit);

    SupplierPurchaseResponse create(UUID shopId, SupplierPurchaseCreateRequest request);

    SupplierPurchaseResponse updateStatus(UUID id, SupplierPurchaseStatus status);
}
