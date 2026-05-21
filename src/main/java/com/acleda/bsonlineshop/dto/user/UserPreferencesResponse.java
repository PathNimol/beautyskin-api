package com.acleda.bsonlineshop.dto.user;

import com.acleda.bsonlineshop.entity.UserPreferences;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserPreferencesResponse {
    private UUID id;
    private boolean darkMode;
    private boolean orderUpdates;
    private boolean promotions;
    private boolean lowStockAlerts;
    private boolean emailNotifications;

    public static UserPreferencesResponse from(UserPreferences prefs) {
        return UserPreferencesResponse.builder()
                .id(prefs.getId())
                .darkMode(prefs.isDarkMode())
                .orderUpdates(prefs.isOrderUpdates())
                .promotions(prefs.isPromotions())
                .lowStockAlerts(prefs.isLowStockAlerts())
                .emailNotifications(prefs.isEmailNotifications())
                .build();
    }
}
