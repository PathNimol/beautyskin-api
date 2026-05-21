package com.acleda.bsonlineshop.dto.chat;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatRoomResponse {
    private UUID id;
    private String name;
    private String roomType;
    private String allowedRoles;
}
