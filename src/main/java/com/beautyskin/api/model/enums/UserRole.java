package com.beautyskin.api.model.enums;

public enum UserRole {
  ADMIN,
  OWNER,
  STAFF,
  CUSTOMER;

  /** API / UI role string (lowercase). */
  public String toApiValue() {
    return name().toLowerCase();
  }

  public static UserRole fromApiValue(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Role is required");
    }
    String normalized = value.trim().toUpperCase();
    if ("BUYER".equals(normalized) || "CUSTOMER".equals(normalized)) {
      return CUSTOMER;
    }
    return UserRole.valueOf(normalized);
  }
}
