package com.acleda.bsonlineshop.dto.user;

import lombok.Data;

/** Partial update — only non-null fields are applied. */
@Data
public class UserPreferencesUpdateRequest {
    private Boolean darkMode;
    private Boolean orderUpdates;
    private Boolean promotions;
    private Boolean lowStockAlerts;
    private Boolean expiryAlerts;
    private Boolean reviewAlerts;
    private Boolean emailNotifications;
}
