package com.acleda.bsonlineshop.controller;

import com.acleda.bsonlineshop.dto.common.ApiResponse;
import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.entity.DirectMessage;
import com.acleda.bsonlineshop.entity.DirectMessageThread;
import com.acleda.bsonlineshop.exception.ResourceNotFoundException;
import com.acleda.bsonlineshop.repository.DirectMessageRepository;
import com.acleda.bsonlineshop.repository.DirectMessageThreadRepository;
import com.acleda.bsonlineshop.security.SecurityUtils;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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

    private final DirectMessageThreadRepository threadRepository;
    private final DirectMessageRepository messageRepository;

    @Operation(summary = "List DM threads", description = "Direct message threads where the current user is a participant.")
    @GetMapping("/threads")
    public ApiResponse<List<DirectMessageThread>> threads() {
        return ApiResponse.success(threadRepository.findForUser(SecurityUtils.currentUserId()));
    }

    @Operation(summary = "List DM messages", description = "Paginated messages in a thread the user can access.")
    @GetMapping("/threads/{threadId}")
    public ApiResponse<PageResponse<DirectMessage>> messages(
            @PathVariable UUID threadId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int limit) {
        return ApiResponse.success(PageResponse.from(
                messageRepository.findByThread_IdAndDeletedFalseOrderByCreatedAtAsc(
                        threadId, PageRequest.of(Math.max(page - 1, 0), limit))));
    }

    @Operation(summary = "Send direct message", description = "Send or start a DM to recipientId with content; creates a thread if needed.")
    @PostMapping
    public ApiResponse<DirectMessage> send(@RequestBody Map<String, String> body) {
        UUID recipientId = UUID.fromString(body.get("recipientId"));
        DirectMessageThread thread = threadRepository.findForUser(SecurityUtils.currentUserId()).stream()
                .filter(t -> t.getParticipantOneId().equals(recipientId)
                        || t.getParticipantTwoId().equals(recipientId))
                .findFirst()
                .orElseGet(() -> {
                    DirectMessageThread t = new DirectMessageThread();
                    t.setParticipantOneId(SecurityUtils.currentUserId());
                    t.setParticipantTwoId(recipientId);
                    t.setSubject("Direct message");
                    return threadRepository.save(t);
                });
        DirectMessage msg = new DirectMessage();
        msg.setThread(thread);
        msg.setSenderId(SecurityUtils.currentUserId());
        msg.setRecipientId(recipientId);
        msg.setContent(body.get("content"));
        msg.setRead(false);
        return ApiResponse.success(messageRepository.save(msg));
    }

    @Operation(summary = "Mark DMs read", description = "Mark unread messages in a thread as read for the current user (recipient).")
    @PatchMapping("/read")
    public ApiResponse<Void> markRead(@RequestBody Map<String, String> body) {
        UUID threadId = UUID.fromString(body.get("threadId"));
        messageRepository.findByThread_IdAndDeletedFalseOrderByCreatedAtAsc(threadId, PageRequest.of(0, 100))
                .forEach(m -> {
                    if (m.getRecipientId().equals(SecurityUtils.currentUserId())) {
                        m.setRead(true);
                        messageRepository.save(m);
                    }
                });
        return ApiResponse.success("Marked as read", null);
    }
}
