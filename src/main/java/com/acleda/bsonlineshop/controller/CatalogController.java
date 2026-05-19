package com.acleda.bsonlineshop.controller;

import com.acleda.bsonlineshop.dto.common.ApiResponse;
import com.acleda.bsonlineshop.dto.common.ListRequest;
import com.acleda.bsonlineshop.dto.common.Result;
import com.acleda.bsonlineshop.service.ProductService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/catalog")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class CatalogController {

    private final ProductService productService;

    @GetMapping("/featured")
    public ApiResponse<Result<Object>> featured(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int limit) {
        ListRequest request = new ListRequest();
        request.setPageNumber(page);
        request.setSize(limit);
        request.setSortProperty("sold");
        request.setSortDirection("DESC");
        return ApiResponse.success(productService.listCatalog(request));
    }

    @GetMapping("/categories")
    public ApiResponse<List<Map<String, String>>> categories() {
        return ApiResponse.success(List.of(
                Map.of("id", "skincare", "name", "Skincare"),
                Map.of("id", "makeup", "name", "Makeup"),
                Map.of("id", "haircare", "name", "Hair Care"),
                Map.of("id", "fragrance", "name", "Fragrance"),
                Map.of("id", "tools", "name", "Tools & Accessories")));
    }
}