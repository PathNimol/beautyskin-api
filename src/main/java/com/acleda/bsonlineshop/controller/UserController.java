package com.acleda.bsonlineshop.controller;

import com.acleda.bsonlineshop.dto.common.ApiResponse;
import com.acleda.bsonlineshop.dto.common.ShippingAddressDto;
import com.acleda.bsonlineshop.dto.user.UpdateProfileRequest;
import com.acleda.bsonlineshop.dto.user.UserResponse;
import com.acleda.bsonlineshop.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Current user", description = "Return the authenticated user's profile.")
    @GetMapping("/me")
    public ApiResponse<UserResponse> me() {
        return ApiResponse.success(userService.getCurrentUser());
    }

    @Operation(summary = "Update profile", description = "Patch name, phone, avatar, and other profile fields for the current user.")
    @PatchMapping("/me")
    public ApiResponse<UserResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.success("Profile updated", userService.updateProfile(request));
    }

    @Operation(summary = "Get shipping address", description = "Return the current user's default shipping address.")
    @GetMapping("/me/shipping")
    public ApiResponse<ShippingAddressDto> getShipping() {
        return ApiResponse.success(userService.getShipping());
    }

    @Operation(summary = "Update shipping address", description = "Replace or set the current user's shipping address.")
    @PatchMapping("/me/shipping")
    public ApiResponse<ShippingAddressDto> updateShipping(@RequestBody ShippingAddressDto dto) {
        return ApiResponse.success("Shipping updated", userService.updateShipping(dto));
    }
}
