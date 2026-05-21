package com.acleda.bsonlineshop.service.impl;

import com.acleda.bsonlineshop.dto.common.ShippingAddressDto;
import com.acleda.bsonlineshop.dto.user.UpdateProfileRequest;
import com.acleda.bsonlineshop.dto.user.UserResponse;
import com.acleda.bsonlineshop.entity.ShippingAddress;
import com.acleda.bsonlineshop.entity.User;
import com.acleda.bsonlineshop.exception.ResourceNotFoundException;
import com.acleda.bsonlineshop.mapper.EntityMapper;
import com.acleda.bsonlineshop.repository.UserRepository;
import com.acleda.bsonlineshop.security.SecurityUtils;
import com.acleda.bsonlineshop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final EntityMapper entityMapper;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        return entityMapper.toUserResponse(findCurrent());
    }

    @Override
    @Transactional
    public UserResponse updateProfile(UpdateProfileRequest request) {
        User user = findCurrent();
        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getAvatar() != null) user.setAvatar(request.getAvatar());
        System.out.println("AVATAR: "  + user.getAvatar());
        if (request.getAvatarAlt() != null) user.setAvatarAlt(request.getAvatarAlt());
        if (request.getShipping() != null) {
            user.setShipping(entityMapper.toShippingEntity(request.getShipping()));
        }
        return entityMapper.toUserResponse(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public ShippingAddressDto getShipping() {
        User user = findCurrent();
        return user.getShipping() != null ? entityMapper.toShippingDto(user.getShipping()) : new ShippingAddressDto();
    }

    @Override
    @Transactional
    public ShippingAddressDto updateShipping(ShippingAddressDto dto) {
        User user = findCurrent();
        user.setShipping(entityMapper.toShippingEntity(dto));
        userRepository.save(user);
        return entityMapper.toShippingDto(user.getShipping());
    }

    private User findCurrent() {
        return userRepository.findById(SecurityUtils.currentUserId())
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
