package com.acleda.bsonlineshop.controller;

import com.acleda.bsonlineshop.dto.common.ApiResponse;
import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.entity.ChatMessage;
import com.acleda.bsonlineshop.entity.ChatRoom;
import com.acleda.bsonlineshop.exception.ResourceNotFoundException;
import com.acleda.bsonlineshop.repository.ChatMessageRepository;
import com.acleda.bsonlineshop.repository.ChatRoomRepository;
import com.acleda.bsonlineshop.security.SecurityUtils;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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

    private final ChatRoomRepository roomRepository;
    private final ChatMessageRepository messageRepository;

    @GetMapping("/rooms")
    public ApiResponse<List<ChatRoom>> rooms() {
        String role = SecurityUtils.currentRole().name().toLowerCase();
        return ApiResponse.success(roomRepository.findAll().stream()
                .filter(r -> !r.isDeleted())
                .filter(r -> r.getAllowedRoles() == null || r.getAllowedRoles().contains(role))
                .toList());
    }

    @GetMapping("/rooms/{roomId}/messages")
    public ApiResponse<PageResponse<ChatMessage>> messages(
            @PathVariable UUID roomId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int limit) {
        return ApiResponse.success(PageResponse.from(
                messageRepository.findByRoom_IdAndDeletedFalseOrderByCreatedAtAsc(
                        roomId, PageRequest.of(Math.max(page - 1, 0), limit))));
    }

    @PostMapping("/rooms/{roomId}/messages")
    public ApiResponse<ChatMessage> send(@PathVariable UUID roomId, @RequestBody Map<String, String> body) {
        ChatRoom room = roomRepository.findById(roomId).filter(r -> !r.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        ChatMessage msg = new ChatMessage();
        msg.setRoom(room);
        msg.setSenderId(SecurityUtils.currentUserId());
        msg.setSenderName(SecurityUtils.currentUser().getUsername());
        msg.setSenderRole(SecurityUtils.currentRole().name());
        msg.setContent(body.get("content"));
        return ApiResponse.success(messageRepository.save(msg));
    }
}
