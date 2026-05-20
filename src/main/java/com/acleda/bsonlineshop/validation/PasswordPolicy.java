package com.acleda.bsonlineshop.validation;

import java.util.regex.Pattern;

public final class PasswordPolicy {

    public static final int MIN_LENGTH = 8;
    public static final int MAX_LENGTH = 128;

  /** At least one letter and one digit. */
    private static final Pattern PATTERN =
            Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).+$");

    public static final String MESSAGE =
            "Password must be 8–128 characters and include at least one letter and one number";

    private PasswordPolicy() {}

    public static boolean isValid(String password) {
        if (password == null) {
            return false;
        }
        int len = password.length();
        return len >= MIN_LENGTH && len <= MAX_LENGTH && PATTERN.matcher(password).matches();
    }

    public static void requireValid(String password) {
        if (!isValid(password)) {
            throw new com.acleda.bsonlineshop.exception.BusinessException(MESSAGE);
        }
    }
}
