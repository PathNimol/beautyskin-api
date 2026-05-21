package com.acleda.bsonlineshop.service;

import com.acleda.bsonlineshop.dto.chat.ChatMessageCreateRequest;
import com.acleda.bsonlineshop.dto.chat.ChatMessageResponse;
import com.acleda.bsonlineshop.dto.chat.ChatRoomResponse;
import com.acleda.bsonlineshop.dto.common.PageResponse;
import java.util.List;
import java.util.UUID;

public interface ChatService {
    List<ChatRoomResponse> listRooms();

    PageResponse<ChatMessageResponse> listMessages(UUID roomId, int page, int limit);

    ChatMessageResponse sendMessage(UUID roomId, ChatMessageCreateRequest request);
}
