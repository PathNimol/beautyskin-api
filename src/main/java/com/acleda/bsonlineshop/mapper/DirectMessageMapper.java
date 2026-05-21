package com.acleda.bsonlineshop.mapper;

import com.acleda.bsonlineshop.dto.message.DirectMessageResponse;
import com.acleda.bsonlineshop.dto.message.DirectThreadResponse;
import com.acleda.bsonlineshop.entity.DirectMessage;
import com.acleda.bsonlineshop.entity.DirectMessageThread;
import org.springframework.stereotype.Component;

@Component
public class DirectMessageMapper {

    public DirectThreadResponse toThreadResponse(DirectMessageThread t) {
        return DirectThreadResponse.builder()
                .id(t.getId())
                .participantOneId(t.getParticipantOneId())
                .participantTwoId(t.getParticipantTwoId())
                .subject(t.getSubject())
                .build();
    }

    public DirectMessageResponse toMessageResponse(DirectMessage m) {
        return DirectMessageResponse.builder()
                .id(m.getId())
                .threadId(m.getThread() != null ? m.getThread().getId() : null)
                .senderId(m.getSenderId())
                .recipientId(m.getRecipientId())
                .content(m.getContent())
                .read(m.isRead())
                .createdAt(m.getCreatedAt())
                .build();
    }
}
