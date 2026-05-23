package com.acleda.bsonlineshop.service.impl;

import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.dto.user.AdminPasswordResetRequest;
import com.acleda.bsonlineshop.dto.user.CustomerCreateRequest;
import com.acleda.bsonlineshop.dto.user.CustomerStatusRequest;
import com.acleda.bsonlineshop.dto.user.CustomerUpdateRequest;
import com.acleda.bsonlineshop.dto.user.UserResponse;
import com.acleda.bsonlineshop.entity.User;
import com.acleda.bsonlineshop.enums.AccountStatus;
import com.acleda.bsonlineshop.enums.UserRole;
import com.acleda.bsonlineshop.exception.BusinessException;
import com.acleda.bsonlineshop.exception.ResourceNotFoundException;
import com.acleda.bsonlineshop.mapper.EntityMapper;
import com.acleda.bsonlineshop.repository.RefreshTokenRepository;
import com.acleda.bsonlineshop.repository.UserRepository;
import com.acleda.bsonlineshop.service.CustomerService;
import com.acleda.bsonlineshop.validation.PasswordPolicy;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EntityMapper entityMapper;
    private final PasswordEncoder passwordEncoder;

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
        return entityMapper.toUserResponse(findCustomer(id));
    }

    @Override
    @Transactional
    public UserResponse create(CustomerCreateRequest request) {
        PasswordPolicy.requireValid(request.getPassword());
        if (userRepository.existsByEmailIgnoreCaseAndDeletedFalse(request.getEmail())) {
            throw new BusinessException("Email already registered");
        }
        User user = new User();
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPhone(request.getPhone());
        user.setRole(UserRole.CUSTOMER);
        user.setStatus(AccountStatus.ACTIVE);
        user.setEmailVerified(true);
        user.setJoinDate(Instant.now());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        return entityMapper.toUserResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse update(UUID id, CustomerUpdateRequest request) {
        User user = findCustomer(id);
        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            user.setFirstName(request.getFirstName().trim());
        }
        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            user.setLastName(request.getLastName().trim());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String email = request.getEmail().trim().toLowerCase();
            if (!email.equalsIgnoreCase(user.getEmail())
                    && userRepository.existsByEmailIgnoreCaseAndDeletedFalse(email)) {
                throw new BusinessException("Email already registered");
            }
            user.setEmail(email);
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        return entityMapper.toUserResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse updateStatus(UUID id, CustomerStatusRequest request) {
        User user = findCustomer(id);
        user.setStatus(request.getStatus());
        return entityMapper.toUserResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void resetPassword(UUID id, AdminPasswordResetRequest request) {
        PasswordPolicy.requireValid(request.getNewPassword());
        User user = findCustomer(id);
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        refreshTokenRepository.revokeAllActiveByUserId(user.getId());
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        User user = findCustomer(id);
        user.setDeleted(true);
        userRepository.save(user);
        refreshTokenRepository.revokeAllActiveByUserId(user.getId());
    }

    private User findCustomer(UUID id) {
        return userRepository.findById(id).filter(u -> !u.isDeleted() && u.getRole() == UserRole.CUSTOMER)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }
}
