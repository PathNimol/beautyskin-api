package com.acleda.bsonlineshop.enums;

public enum AccountStatus {
    /** Email/password registered but inbox not yet verified via OTP (cannot log in until ACTIVE). */
    PENDING_EMAIL_VERIFICATION,
    ACTIVE,
    INACTIVE,
    SUSPENDED
}
