package com.acleda.bsonlineshop.mapper;

import com.acleda.bsonlineshop.dto.common.ShippingAddressDto;
import com.acleda.bsonlineshop.dto.user.UserResponse;
import com.acleda.bsonlineshop.entity.ShippingAddress;
import com.acleda.bsonlineshop.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EntityMapper {

    @Mapping(target = "shipping", source = "shipping")
    UserResponse toUserResponse(User user);

    ShippingAddressDto toShippingDto(ShippingAddress address);

    ShippingAddress toShippingEntity(ShippingAddressDto dto);
}
