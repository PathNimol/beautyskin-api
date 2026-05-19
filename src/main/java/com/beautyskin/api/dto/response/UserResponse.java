package com.beautyskin.api.dto.response;

import com.beautyskin.api.model.entity.User;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import lombok.Builder;

@Builder
public record UserResponse(
    String id,
    String email,
    String name,
    String firstName,
    String lastName,
    String role,
    String shopId,
    String avatar,
    String avatarAlt,
    String phone,
    String joinDate) {

  private static final DateTimeFormatter JOIN_FORMAT =
      DateTimeFormatter.ofPattern("MMM d, yyyy").withZone(ZoneOffset.UTC);

  public static UserResponse from(User user) {
    String avatar = user.getAvatarUrl() != null ? user.getAvatarUrl() : "";
    return UserResponse.builder()
        .id(user.getId().toString())
        .email(user.getEmail())
        .name(user.getDisplayName())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .role(user.getRole().toApiValue())
        .shopId(user.getShopId())
        .avatar(avatar)
        .avatarAlt(user.getDisplayName())
        .phone(user.getPhone())
        .joinDate(user.getCreatedAt() != null ? JOIN_FORMAT.format(user.getCreatedAt()) : null)
        .build();
  }
}
