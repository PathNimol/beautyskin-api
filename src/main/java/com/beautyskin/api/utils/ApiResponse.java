package com.beautyskin.api.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

  private String status;
  private String message;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private T data;

  private String timestamp;

  public static <T> ApiResponse<T> success(String message, T data, String timestamp) {
    return ApiResponse.<T>builder()
        .status("success")
        .message(message)
        .data(data)
        .timestamp(timestamp)
        .build();
  }

  public static <T> ApiResponse<T> error(String message, String timestamp) {
    return ApiResponse.<T>builder().status("error").message(message).timestamp(timestamp).build();
  }
}
