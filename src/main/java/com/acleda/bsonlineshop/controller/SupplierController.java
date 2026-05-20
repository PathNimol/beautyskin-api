package com.acleda.bsonlineshop.controller;

import com.acleda.bsonlineshop.dto.common.ApiResponse;
import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.entity.Supplier;
import com.acleda.bsonlineshop.repository.SupplierRepository;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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

    private final SupplierRepository supplierRepository;

    @Operation(summary = "List suppliers", description = "Paginated global supplier directory with optional search.")
    @GetMapping
    public ApiResponse<PageResponse<Supplier>> list(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.success(PageResponse.from(
                supplierRepository.search(search, PageRequest.of(Math.max(page - 1, 0), limit))));
    }

    @Operation(summary = "Create supplier", description = "Add a new supplier record (join date and active status set server-side).")
    @PostMapping
    public ApiResponse<Supplier> create(@RequestBody Supplier supplier) {
        supplier.setJoinDate(LocalDate.now());
        supplier.setStatus("active");
        return ApiResponse.success("Supplier created", supplierRepository.save(supplier));
    }
}
