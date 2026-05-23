package com.acleda.bsonlineshop.dto.message;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DirectThreadResponse {
    private UUID id;
    private UUID participantOneId;
    private UUID participantTwoId;
    private String subject;
}
