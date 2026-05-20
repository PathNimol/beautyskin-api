package com.acleda.bsonlineshop.service;

import java.util.Map;
import java.util.UUID;

public interface DashboardService {
    Map<String, Object> adminDashboard();
    Map<String, Object> shopDashboard(UUID shopId);
}
