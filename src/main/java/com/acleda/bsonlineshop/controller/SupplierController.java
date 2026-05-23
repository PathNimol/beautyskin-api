package com.acleda.bsonlineshop.controller;

import com.acleda.bsonlineshop.dto.common.ApiResponse;
import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.dto.supplier.SupplierCreateRequest;
import com.acleda.bsonlineshop.dto.supplier.SupplierResponse;
import com.acleda.bsonlineshop.dto.supplier.SupplierUpdateRequest;
import com.acleda.bsonlineshop.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
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
@RequestMapping("/suppliers")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("@authz.adminOrMerchant()")
public class SupplierController {

    private final SupplierService supplierService;

    @Operation(summary = "List suppliers", description = "Paginated global supplier directory with optional search.")
    @GetMapping
    public ApiResponse<PageResponse<SupplierResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.success(supplierService.list(search, page, limit));
    }

    @Operation(summary = "Create supplier", description = "Add a new supplier record.")
    @PostMapping
    public ApiResponse<SupplierResponse> create(@Valid @RequestBody SupplierCreateRequest request) {
        return ApiResponse.success("Supplier created", supplierService.create(request));
    }

    @Operation(summary = "Update supplier", description = "Update supplier fields by id.")
    @PutMapping("/{id}")
    public ApiResponse<SupplierResponse> update(
            @PathVariable UUID id, @Valid @RequestBody SupplierUpdateRequest request) {
        return ApiResponse.success("Supplier updated", supplierService.update(id, request));
    }

    @Operation(summary = "Delete supplier", description = "Soft-delete a supplier.")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        supplierService.delete(id);
        return ApiResponse.success("Supplier deleted", null);
    }
}
