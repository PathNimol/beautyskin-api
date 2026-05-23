package com.acleda.bsonlineshop.controller;

import com.acleda.bsonlineshop.dto.common.ApiResponse;
import com.acleda.bsonlineshop.dto.common.ListRequest;
import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.dto.common.PageAbleResponse;
import com.acleda.bsonlineshop.dto.product.ProductCreateRequest;
import com.acleda.bsonlineshop.dto.product.ProductResponse;
import com.acleda.bsonlineshop.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @Operation(summary = "List products (storefront)", description = "Public catalog search with filters (category, price), sort, and pagination. Does not require auth in production config.")
    @GetMapping
    public ApiResponse<PageAbleResponse<ProductResponse>> list(
                                              @RequestParam(required = false) String search,
                                              @RequestParam(required = false) String category,
                                              @RequestParam(required = false) String minPrice,
                                              @RequestParam(required = false) String maxPrice,
                                              @RequestParam(defaultValue = "sold") String sort,
                                              @RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "24") int limit) {

        ListRequest request = new ListRequest();
        request.setPageNumber(Math.max(page - 1, 0));
        request.setSize(Math.min(Math.max(limit, 1), 48));
        request.setSearch(search);
        applyCatalogSort(request, sort);
        if (category != null && !category.isBlank()) {
            Map<String, String> filters = new HashMap<>();
            filters.put("category", category);
            request.setFilters(filters);
        }
        if (minPrice != null && !minPrice.isBlank()) {
            request.setMinPrice(new BigDecimal(minPrice));
        }
        if (maxPrice != null && !maxPrice.isBlank()) {
            request.setMaxPrice(new BigDecimal(maxPrice));
        }
        return ApiResponse.success(productService.listCatalog(request));
    }

    private static void applyCatalogSort(ListRequest request, String sort) {
        String key = sort == null ? "featured" : sort.toLowerCase();
        switch (key) {
            case "price_asc" -> {
                request.setSortProperty("price");
                request.setSortDirection("ASC");
            }
            case "price_desc" -> {
                request.setSortProperty("price");
                request.setSortDirection("DESC");
            }
            case "rating" -> {
                request.setSortProperty("rating");
                request.setSortDirection("DESC");
            }
            case "newest" -> {
                request.setSortProperty("createdAt");
                request.setSortDirection("DESC");
            }
            default -> {
                request.setSortProperty("sold");
                request.setSortDirection("DESC");
            }
        }
    }

    @Operation(summary = "Get product by id", description = "Return a single product with images and details for the product page.")
    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> get(@PathVariable UUID id) {
        return ApiResponse.success(productService.getById(id));
    }

    @Operation(summary = "Merchant product list", description = "Paginated products for a shop (owner/staff/admin). Filter by search, category, status.")
    @GetMapping("/merchant")
    @PreAuthorize("@authz.admin() || @authz.merchantInShop(#shopId)")
    public ApiResponse<PageResponse<ProductResponse>> merchantList(
            @RequestParam UUID shopId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "8") int limit) {
        return ApiResponse.success(productService.listMerchant(shopId, search, category, status, page, limit));
    }

    @Operation(summary = "Create product", description = "Add a new product to the given shop. Caller must be allowed for that shop.")
    @PostMapping("/shops/{shopId}")
    @PreAuthorize("@authz.admin() || @authz.merchantInShop(#shopId)")
    public ApiResponse<ProductResponse> create(
            @PathVariable UUID shopId, @Valid @RequestBody ProductCreateRequest request) {
        return ApiResponse.success("Product created", productService.create(shopId, request));
    }

    @Operation(summary = "Update product", description = "Replace product fields for an existing product in the shop.")
    @PutMapping("/shops/{shopId}/{productId}")
    @PreAuthorize("@authz.admin() || @authz.merchantInShop(#shopId)")
    public ApiResponse<ProductResponse> update(
            @PathVariable UUID shopId,
            @PathVariable UUID productId,
            @Valid @RequestBody ProductCreateRequest request) {
        return ApiResponse.success("Product updated", productService.update(shopId, productId, request));
    }

    @Operation(summary = "Delete product", description = "Soft-delete a product belonging to the shop.")
    @DeleteMapping("/shops/{shopId}/{productId}")
    @PreAuthorize("@authz.admin() || @authz.merchantInShop(#shopId)")
    public ApiResponse<Void> delete(@PathVariable UUID shopId, @PathVariable UUID productId) {
        productService.delete(shopId, productId);
        return ApiResponse.success("Product deleted", null);
    }
}
