package com.acleda.bsonlineshop.controller;

import com.acleda.bsonlineshop.dto.common.ApiResponse;
import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.dto.user.AdminPasswordResetRequest;
import com.acleda.bsonlineshop.dto.user.CustomerCreateRequest;
import com.acleda.bsonlineshop.dto.user.CustomerStatusRequest;
import com.acleda.bsonlineshop.dto.user.CustomerUpdateRequest;
import com.acleda.bsonlineshop.dto.user.UserResponse;
import com.acleda.bsonlineshop.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class CustomerController {

    private final CustomerService customerService;

    @Operation(summary = "List customers", description = "Paginated customer (CUSTOMER role) directory.")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','OWNER')")
    public ApiResponse<PageResponse<UserResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.success(customerService.list(search, page, limit));
    }

    @Operation(summary = "Get customer", description = "Return a single customer user by id.")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OWNER')")
    public ApiResponse<UserResponse> get(@PathVariable UUID id) {
        return ApiResponse.success(customerService.get(id));
    }

    @Operation(summary = "Create customer", description = "Admin creates an ACTIVE customer account with password.")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> create(@Valid @RequestBody CustomerCreateRequest request) {
        return ApiResponse.success("Customer created", customerService.create(request));
    }

    @Operation(summary = "Update customer", description = "Admin updates customer profile fields.")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> update(
            @PathVariable UUID id, @Valid @RequestBody CustomerUpdateRequest request) {
        return ApiResponse.success("Customer updated", customerService.update(id, request));
    }

    @Operation(summary = "Update customer status", description = "Activate, suspend, or deactivate a customer account.")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','OWNER')")
    public ApiResponse<UserResponse> updateStatus(
            @PathVariable UUID id, @Valid @RequestBody CustomerStatusRequest request) {
        return ApiResponse.success("Status updated", customerService.updateStatus(id, request));
    }

    @Operation(summary = "Reset customer password", description = "Admin sets a new password and revokes refresh tokens.")
    @PatchMapping("/{id}/password")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> resetPassword(
            @PathVariable UUID id, @Valid @RequestBody AdminPasswordResetRequest request) {
        customerService.resetPassword(id, request);
        return ApiResponse.success("Password updated", null);
    }

    @Operation(summary = "Delete customer", description = "Soft-delete a customer account.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        customerService.delete(id);
        return ApiResponse.success("Customer deleted", null);
    }
}
