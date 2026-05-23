package com.acleda.bsonlineshop.mapper;

import com.acleda.bsonlineshop.dto.chat.ChatMessageResponse;
import com.acleda.bsonlineshop.dto.chat.ChatRoomResponse;
import com.acleda.bsonlineshop.entity.ChatMessage;
import com.acleda.bsonlineshop.entity.ChatRoom;
import org.springframework.stereotype.Component;

@Component
public class ChatMapper {

    public ChatRoomResponse toRoomResponse(ChatRoom r) {
        return ChatRoomResponse.builder()
                .id(r.getId())
                .name(r.getName())
                .roomType(r.getRoomType())
                .allowedRoles(r.getAllowedRoles())
                .build();
    }

    public ChatMessageResponse toMessageResponse(ChatMessage m) {
        return ChatMessageResponse.builder()
                .id(m.getId())
                .roomId(m.getRoom() != null ? m.getRoom().getId() : null)
                .senderId(m.getSenderId())
                .senderName(m.getSenderName())
                .senderRole(m.getSenderRole())
                .content(m.getContent())
                .createdAt(m.getCreatedAt())
                .build();
    }
}
