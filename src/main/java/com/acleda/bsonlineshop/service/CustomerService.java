package com.acleda.bsonlineshop.service;

import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.dto.user.AdminPasswordResetRequest;
import com.acleda.bsonlineshop.dto.user.CustomerCreateRequest;
import com.acleda.bsonlineshop.dto.user.CustomerStatusRequest;
import com.acleda.bsonlineshop.dto.user.CustomerUpdateRequest;
import com.acleda.bsonlineshop.dto.user.UserResponse;
import java.util.UUID;

public interface CustomerService {
    PageResponse<UserResponse> list(String search, int page, int limit);

    UserResponse get(UUID id);

    UserResponse create(CustomerCreateRequest request);

    UserResponse update(UUID id, CustomerUpdateRequest request);

    UserResponse updateStatus(UUID id, CustomerStatusRequest request);

    void resetPassword(UUID id, AdminPasswordResetRequest request);

    void delete(UUID id);
}
