package com.acleda.bsonlineshop.service;

import java.util.Map;
import java.util.UUID;

public interface AnalyticsService {
    Map<String, Object> summary(UUID shopId, String range);
}
