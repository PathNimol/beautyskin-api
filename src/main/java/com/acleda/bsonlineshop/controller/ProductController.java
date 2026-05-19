package com.acleda.bsonlineshop.controller;

import com.acleda.bsonlineshop.dto.common.ApiResponse;
import com.acleda.bsonlineshop.dto.common.ListRequest;
import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.dto.common.Result;
import com.acleda.bsonlineshop.dto.product.ProductCreateRequest;
import com.acleda.bsonlineshop.dto.product.ProductResponse;
import com.acleda.bsonlineshop.service.ProductService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

import java.util.Map;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ApiResponse<Result<Object>> list(  // ← change PageResponse<ProductResponse> to Result<Object>
                                              @RequestParam(required = false) String search,
                                              @RequestParam(required = false) String category,
                                              @RequestParam(required = false) String minPrice,
                                              @RequestParam(required = false) String maxPrice,
                                              @RequestParam(defaultValue = "sold") String sort,
                                              @RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "24") int limit) {

        ListRequest request = new ListRequest();
        request.setPageNumber(Math.max(page - 1, 0));
        request.setSize(limit);
        request.setSearch(search);
        request.setSortProperty("sold");
        request.setSortDirection("DESC");
        if (category != null) request.setFilters(Map.of("category", category));
        return ApiResponse.success(productService.listCatalog(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> get(@PathVariable UUID id) {
        return ApiResponse.success(productService.getById(id));
    }

    @GetMapping("/merchant")
    public ApiResponse<PageResponse<ProductResponse>> merchantList(
            @RequestParam UUID shopId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "8") int limit) {
        return ApiResponse.success(productService.listMerchant(shopId, search, category, status, page, limit));
    }

    @PostMapping("/shops/{shopId}")
    public ApiResponse<ProductResponse> create(
            @PathVariable UUID shopId, @Valid @RequestBody ProductCreateRequest request) {
        return ApiResponse.success("Product created", productService.create(shopId, request));
    }

    @PutMapping("/shops/{shopId}/{productId}")
    public ApiResponse<ProductResponse> update(
            @PathVariable UUID shopId,
            @PathVariable UUID productId,
            @Valid @RequestBody ProductCreateRequest request) {
        return ApiResponse.success("Product updated", productService.update(shopId, productId, request));
    }

    @DeleteMapping("/shops/{shopId}/{productId}")
    public ApiResponse<Void> delete(@PathVariable UUID shopId, @PathVariable UUID productId) {
        productService.delete(shopId, productId);
        return ApiResponse.success("Product deleted", null);
    }
}
