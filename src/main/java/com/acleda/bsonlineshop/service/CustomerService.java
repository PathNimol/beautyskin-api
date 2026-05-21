package com.acleda.bsonlineshop.service;

import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.dto.user.CustomerStatusRequest;
import com.acleda.bsonlineshop.dto.user.UserResponse;
import com.acleda.bsonlineshop.enums.AccountStatus;
import java.util.UUID;

public interface CustomerService {
    PageResponse<UserResponse> list(String search, int page, int limit);

    UserResponse get(UUID id);

    UserResponse updateStatus(UUID id, CustomerStatusRequest request);
}
