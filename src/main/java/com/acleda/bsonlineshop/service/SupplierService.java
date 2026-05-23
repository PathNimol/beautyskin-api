package com.acleda.bsonlineshop.service;

import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.dto.supplier.SupplierCreateRequest;
import com.acleda.bsonlineshop.dto.supplier.SupplierResponse;
import com.acleda.bsonlineshop.dto.supplier.SupplierUpdateRequest;
import java.util.UUID;

public interface SupplierService {
    PageResponse<SupplierResponse> list(String search, int page, int limit);

    SupplierResponse create(SupplierCreateRequest request);

    SupplierResponse update(UUID id, SupplierUpdateRequest request);

    void delete(UUID id);
}
