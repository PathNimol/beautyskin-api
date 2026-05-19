package com.beautyskin.api.utils;

import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class BaseResponse {

  protected <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
    return ResponseEntity.ok(ApiResponse.success(message, data, Instant.now().toString()));
  }

  protected <T> ResponseEntity<ApiResponse<T>> created(String message, T data) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(message, data, Instant.now().toString()));
  }

  protected <T> ResponseEntity<ApiResponse<T>> error(HttpStatus status, String message) {
    return ResponseEntity.status(status)
        .body(ApiResponse.error(message, Instant.now().toString()));
  }
}
