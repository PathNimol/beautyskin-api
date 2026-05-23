package com.acleda.bsonlineshop.controller;

import com.acleda.bsonlineshop.dto.common.ApiResponse;
import com.acleda.bsonlineshop.service.DashboardService;
import java.util.Map;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Admin dashboard", description = "Platform-wide metrics for administrators (ADMIN role required).")
    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> adminDashboard() {
        return ApiResponse.success(dashboardService.adminDashboard());
    }

    @Operation(summary = "Shop dashboard", description = "Merchant KPIs for a shop. Optional shopId; non-admins are scoped to their assigned shop.")
    @GetMapping("/dashboard")
    @PreAuthorize("@authz.adminOrMerchant()")
    public ApiResponse<Map<String, Object>> shopDashboard(@RequestParam(required = false) UUID shopId) {
        return ApiResponse.success(dashboardService.shopDashboard(shopId));
    }
}
