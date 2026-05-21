package com.acleda.bsonlineshop.service;

import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.dto.message.DirectMessageCreateRequest;
import com.acleda.bsonlineshop.dto.message.DirectMessageResponse;
import com.acleda.bsonlineshop.dto.message.DirectThreadResponse;
import com.acleda.bsonlineshop.dto.message.MarkThreadReadRequest;
import java.util.List;
import java.util.UUID;

public interface DirectMessageService {
    List<DirectThreadResponse> listThreads();

    PageResponse<DirectMessageResponse> listMessages(UUID threadId, int page, int limit);

    DirectMessageResponse send(DirectMessageCreateRequest request);

    void markRead(MarkThreadReadRequest request);
}
