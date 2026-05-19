package com.acleda.bsonlineshop.dto.common;

import lombok.Getter;

@Getter
public class Result<T> {
    private final boolean success;
    private final String message;
    private final T data;

    private Result(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> of(T data) {
        return new Result<>(true, "Success", data);
    }

    public static <T> Result<T> of(String message, T data) {
        return new Result<>(true, message, data);
    }

    public static <T> Result<T> error(String message) {
        return new Result<>(false, message, null);
    }
}