package com.acleda.bsonlineshop.controller;

import com.acleda.bsonlineshop.dto.common.ApiResponse;
import com.acleda.bsonlineshop.dto.user.UserPreferencesResponse;
import com.acleda.bsonlineshop.dto.user.UserPreferencesUpdateRequest;
import com.acleda.bsonlineshop.entity.User;
import com.acleda.bsonlineshop.entity.UserPreferences;
import com.acleda.bsonlineshop.exception.ResourceNotFoundException;
import com.acleda.bsonlineshop.repository.UserPreferencesRepository;
import com.acleda.bsonlineshop.repository.UserRepository;
import com.acleda.bsonlineshop.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/me/preferences")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UserPreferencesController {

    private final UserPreferencesRepository preferencesRepository;
    private final UserRepository userRepository;

    @Operation(summary = "Get preferences", description = "Return notification and UI preferences for the current user (creates defaults if missing).")
    @GetMapping
    public ApiResponse<UserPreferencesResponse> get() {
        return ApiResponse.success(UserPreferencesResponse.from(getOrCreate()));
    }

    @Operation(summary = "Update preferences", description = "Update dark mode, email, order, promotion, and stock alert toggles.")
    @PatchMapping
    public ApiResponse<UserPreferencesResponse> update(@RequestBody UserPreferencesUpdateRequest body) {
        UserPreferences prefs = getOrCreate();
        if (body.getDarkMode() != null) {
            prefs.setDarkMode(body.getDarkMode());
        }
        if (body.getOrderUpdates() != null) {
            prefs.setOrderUpdates(body.getOrderUpdates());
        }
        if (body.getPromotions() != null) {
            prefs.setPromotions(body.getPromotions());
        }
        if (body.getLowStockAlerts() != null) {
            prefs.setLowStockAlerts(body.getLowStockAlerts());
        }
        if (body.getExpiryAlerts() != null) {
            prefs.setExpiryAlerts(body.getExpiryAlerts());
        }
        if (body.getReviewAlerts() != null) {
            prefs.setReviewAlerts(body.getReviewAlerts());
        }
        if (body.getEmailNotifications() != null) {
            prefs.setEmailNotifications(body.getEmailNotifications());
        }
        return ApiResponse.success(
                "Preferences updated", UserPreferencesResponse.from(preferencesRepository.save(prefs)));
    }

    private UserPreferences getOrCreate() {
        User user = userRepository.findById(SecurityUtils.currentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return preferencesRepository.findByUserAndDeletedFalse(user).orElseGet(() -> {
            UserPreferences p = new UserPreferences();
            p.setUser(user);
            p.setDarkMode(false);
            p.setOrderUpdates(true);
            p.setPromotions(true);
            p.setLowStockAlerts(true);
            p.setExpiryAlerts(true);
            p.setReviewAlerts(true);
            p.setEmailNotifications(true);
            return preferencesRepository.save(p);
        });
    }
}
