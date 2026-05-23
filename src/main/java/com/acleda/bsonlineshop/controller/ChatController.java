package com.acleda.bsonlineshop.controller;

import com.acleda.bsonlineshop.dto.chat.ChatMessageCreateRequest;
import com.acleda.bsonlineshop.dto.chat.ChatMessageResponse;
import com.acleda.bsonlineshop.dto.chat.ChatRoomResponse;
import com.acleda.bsonlineshop.dto.common.ApiResponse;
import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ChatController {

    private final ChatService chatService;

    @Operation(summary = "List chat rooms", description = "Rooms visible to the current user role.")
    @GetMapping("/rooms")
    public ApiResponse<List<ChatRoomResponse>> rooms() {
        return ApiResponse.success(chatService.listRooms());
    }

    @Operation(summary = "List chat messages", description = "Paginated messages in a chat room.")
    @GetMapping("/rooms/{roomId}/messages")
    public ApiResponse<PageResponse<ChatMessageResponse>> messages(
            @PathVariable UUID roomId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int limit) {
        return ApiResponse.success(chatService.listMessages(roomId, page, limit));
    }

    @Operation(summary = "Send chat message", description = "Post a message to a room.")
    @PostMapping("/rooms/{roomId}/messages")
    public ApiResponse<ChatMessageResponse> send(
            @PathVariable UUID roomId, @Valid @RequestBody ChatMessageCreateRequest request) {
        return ApiResponse.success(chatService.sendMessage(roomId, request));
    }
}
