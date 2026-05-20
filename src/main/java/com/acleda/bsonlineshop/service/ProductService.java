package com.acleda.bsonlineshop.service;

import com.acleda.bsonlineshop.dto.common.ListRequest;
import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.dto.common.Result;
import com.acleda.bsonlineshop.dto.product.ProductCreateRequest;
import com.acleda.bsonlineshop.dto.product.ProductResponse;
import java.util.UUID;

public interface ProductService {
     ProductResponse getById(UUID id);
    PageResponse<ProductResponse> listMerchant(UUID shopId, String search, String category, String status, int page, int limit);
    ProductResponse create(UUID shopId, ProductCreateRequest request);
    ProductResponse update(UUID shopId, UUID productId, ProductCreateRequest request);
    void delete(UUID shopId, UUID productId);

    Result<Object> listCatalog(ListRequest request);
}
