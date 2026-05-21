package com.acleda.bsonlineshop.controller;

import com.acleda.bsonlineshop.dto.common.ApiResponse;
import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.dto.message.DirectMessageCreateRequest;
import com.acleda.bsonlineshop.dto.message.DirectMessageResponse;
import com.acleda.bsonlineshop.dto.message.DirectThreadResponse;
import com.acleda.bsonlineshop.dto.message.MarkThreadReadRequest;
import com.acleda.bsonlineshop.service.DirectMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class DirectMessageController {

    private final DirectMessageService directMessageService;

    @Operation(summary = "List DM threads", description = "Direct message threads where the current user is a participant.")
    @GetMapping("/threads")
    public ApiResponse<List<DirectThreadResponse>> threads() {
        return ApiResponse.success(directMessageService.listThreads());
    }

    @Operation(summary = "List DM messages", description = "Paginated messages in a thread the user can access.")
    @GetMapping("/threads/{threadId}")
    public ApiResponse<PageResponse<DirectMessageResponse>> messages(
            @PathVariable UUID threadId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int limit) {
        return ApiResponse.success(directMessageService.listMessages(threadId, page, limit));
    }

    @Operation(summary = "Send direct message", description = "Send or start a DM to recipientId with content.")
    @PostMapping
    public ApiResponse<DirectMessageResponse> send(@Valid @RequestBody DirectMessageCreateRequest request) {
        return ApiResponse.success(directMessageService.send(request));
    }

    @Operation(summary = "Mark DMs read", description = "Mark unread messages in a thread as read for the current user.")
    @PatchMapping("/read")
    public ApiResponse<Void> markRead(@Valid @RequestBody MarkThreadReadRequest request) {
        directMessageService.markRead(request);
        return ApiResponse.success("Marked as read", null);
    }
}
