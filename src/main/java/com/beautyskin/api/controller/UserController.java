package com.beautyskin.api.controller;

import com.beautyskin.api.dto.response.UserResponse;
import com.beautyskin.api.model.entity.User;
import com.beautyskin.api.service.AuthService;
import com.beautyskin.api.utils.ApiResponse;
import com.beautyskin.api.utils.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Authenticated user profile")
@SecurityRequirement(name = "bearerAuth")
public class UserController extends BaseResponse {

  private final AuthService authService;

  @GetMapping("/me")
  @Operation(summary = "Get current user profile")
  public ResponseEntity<ApiResponse<UserResponse>> me(@AuthenticationPrincipal User user) {
    return ok("Profile retrieved", authService.getCurrentUser(user));
  }
}
