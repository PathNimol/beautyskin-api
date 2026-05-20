package com.acleda.bsonlineshop.controller;

import com.acleda.bsonlineshop.dto.common.ApiResponse;
import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.dto.user.UserResponse;
import com.acleda.bsonlineshop.entity.User;
import com.acleda.bsonlineshop.enums.UserRole;
import com.acleda.bsonlineshop.exception.ResourceNotFoundException;
import com.acleda.bsonlineshop.mapper.EntityMapper;
import com.acleda.bsonlineshop.repository.UserRepository;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class CustomerController {

    private final UserRepository userRepository;
    private final EntityMapper entityMapper;

    @Operation(summary = "List customers", description = "Paginated customer (CUSTOMER role) directory for admin or owner.")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','OWNER')")
    public ApiResponse<PageResponse<UserResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        var result = userRepository.searchCustomers(UserRole.CUSTOMER, search, PageRequest.of(Math.max(page - 1, 0), limit));
        return ApiResponse.success(PageResponse.from(result.map(entityMapper::toUserResponse)));
    }

    @Operation(summary = "Get customer", description = "Return a single customer user by id (ADMIN or OWNER).")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OWNER')")
    public ApiResponse<UserResponse> get(@PathVariable UUID id) {
        User user = userRepository.findById(id).filter(u -> !u.isDeleted() && u.getRole() == UserRole.CUSTOMER)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        return ApiResponse.success(entityMapper.toUserResponse(user));
    }
}
