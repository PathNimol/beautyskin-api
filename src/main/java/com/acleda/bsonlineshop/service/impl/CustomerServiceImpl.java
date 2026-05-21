package com.acleda.bsonlineshop.service.impl;

import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.dto.user.CustomerStatusRequest;
import com.acleda.bsonlineshop.dto.user.UserResponse;
import com.acleda.bsonlineshop.entity.User;
import com.acleda.bsonlineshop.enums.UserRole;
import com.acleda.bsonlineshop.exception.ResourceNotFoundException;
import com.acleda.bsonlineshop.mapper.EntityMapper;
import com.acleda.bsonlineshop.repository.UserRepository;
import com.acleda.bsonlineshop.service.CustomerService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final UserRepository userRepository;
    private final EntityMapper entityMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> list(String search, int page, int limit) {
        return PageResponse.from(userRepository
                .searchCustomers(UserRole.CUSTOMER, search, PageRequest.of(Math.max(page - 1, 0), limit))
                .map(entityMapper::toUserResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse get(UUID id) {
        User user = userRepository.findById(id).filter(u -> !u.isDeleted() && u.getRole() == UserRole.CUSTOMER)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        return entityMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateStatus(UUID id, CustomerStatusRequest request) {
        User user = userRepository.findById(id).filter(u -> !u.isDeleted() && u.getRole() == UserRole.CUSTOMER)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        user.setStatus(request.getStatus());
        return entityMapper.toUserResponse(userRepository.save(user));
    }
}
