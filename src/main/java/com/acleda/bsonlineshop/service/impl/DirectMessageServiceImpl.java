package com.acleda.bsonlineshop.service.impl;

import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.dto.message.DirectMessageCreateRequest;
import com.acleda.bsonlineshop.dto.message.DirectMessageResponse;
import com.acleda.bsonlineshop.dto.message.DirectThreadResponse;
import com.acleda.bsonlineshop.dto.message.MarkThreadReadRequest;
import com.acleda.bsonlineshop.entity.DirectMessage;
import com.acleda.bsonlineshop.entity.DirectMessageThread;
import com.acleda.bsonlineshop.mapper.DirectMessageMapper;
import com.acleda.bsonlineshop.repository.DirectMessageRepository;
import com.acleda.bsonlineshop.repository.DirectMessageThreadRepository;
import com.acleda.bsonlineshop.security.SecurityUtils;
import com.acleda.bsonlineshop.service.DirectMessageService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DirectMessageServiceImpl implements DirectMessageService {

    private final DirectMessageThreadRepository threadRepository;
    private final DirectMessageRepository messageRepository;
    private final DirectMessageMapper directMessageMapper;

    @Override
    @Transactional(readOnly = true)
    public List<DirectThreadResponse> listThreads() {
        return threadRepository.findForUser(SecurityUtils.currentUserId()).stream()
                .map(directMessageMapper::toThreadResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DirectMessageResponse> listMessages(UUID threadId, int page, int limit) {
        return PageResponse.from(messageRepository
                .findByThread_IdAndDeletedFalseOrderByCreatedAtAsc(
                        threadId, PageRequest.of(Math.max(page - 1, 0), limit))
                .map(directMessageMapper::toMessageResponse));
    }

    @Override
    @Transactional
    public DirectMessageResponse send(DirectMessageCreateRequest request) {
        UUID recipientId = request.getRecipientId();
        UUID currentId = SecurityUtils.currentUserId();
        DirectMessageThread thread = threadRepository.findForUser(currentId).stream()
                .filter(t -> t.getParticipantOneId().equals(recipientId)
                        || t.getParticipantTwoId().equals(recipientId))
                .findFirst()
                .orElseGet(() -> {
                    DirectMessageThread t = new DirectMessageThread();
                    t.setParticipantOneId(currentId);
                    t.setParticipantTwoId(recipientId);
                    t.setSubject("Direct message");
                    return threadRepository.save(t);
                });
        DirectMessage msg = new DirectMessage();
        msg.setThread(thread);
        msg.setSenderId(currentId);
        msg.setRecipientId(recipientId);
        msg.setContent(request.getContent());
        msg.setRead(false);
        return directMessageMapper.toMessageResponse(messageRepository.save(msg));
    }

    @Override
    @Transactional
    public void markRead(MarkThreadReadRequest request) {
        UUID currentId = SecurityUtils.currentUserId();
        messageRepository
                .findByThread_IdAndDeletedFalseOrderByCreatedAtAsc(
                        request.getThreadId(), PageRequest.of(0, 500))
                .forEach(m -> {
                    if (m.getRecipientId().equals(currentId)) {
                        m.setRead(true);
                        messageRepository.save(m);
                    }
                });
    }
}
