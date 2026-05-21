package com.acleda.bsonlineshop.service.impl;

import com.acleda.bsonlineshop.dto.chat.ChatMessageCreateRequest;
import com.acleda.bsonlineshop.dto.chat.ChatMessageResponse;
import com.acleda.bsonlineshop.dto.chat.ChatRoomResponse;
import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.entity.ChatMessage;
import com.acleda.bsonlineshop.entity.ChatRoom;
import com.acleda.bsonlineshop.exception.ResourceNotFoundException;
import com.acleda.bsonlineshop.mapper.ChatMapper;
import com.acleda.bsonlineshop.repository.ChatMessageRepository;
import com.acleda.bsonlineshop.repository.ChatRoomRepository;
import com.acleda.bsonlineshop.security.SecurityUtils;
import com.acleda.bsonlineshop.service.ChatService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRoomRepository roomRepository;
    private final ChatMessageRepository messageRepository;
    private final ChatMapper chatMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ChatRoomResponse> listRooms() {
        String role = SecurityUtils.currentRole().name().toLowerCase();
        return roomRepository.findAll().stream()
                .filter(r -> !r.isDeleted())
                .filter(r -> r.getAllowedRoles() == null || r.getAllowedRoles().contains(role))
                .map(chatMapper::toRoomResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ChatMessageResponse> listMessages(UUID roomId, int page, int limit) {
        return PageResponse.from(messageRepository
                .findByRoom_IdAndDeletedFalseOrderByCreatedAtAsc(roomId, PageRequest.of(Math.max(page - 1, 0), limit))
                .map(chatMapper::toMessageResponse));
    }

    @Override
    @Transactional
    public ChatMessageResponse sendMessage(UUID roomId, ChatMessageCreateRequest request) {
        ChatRoom room = roomRepository.findById(roomId).filter(r -> !r.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        ChatMessage msg = new ChatMessage();
        msg.setRoom(room);
        msg.setSenderId(SecurityUtils.currentUserId());
        msg.setSenderName(SecurityUtils.currentUser().getUsername());
        msg.setSenderRole(SecurityUtils.currentRole().name());
        msg.setContent(request.getContent());
        return chatMapper.toMessageResponse(messageRepository.save(msg));
    }
}
