package com.acleda.bsonlineshop.service;

import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.dto.revoke.RevokeCreateRequest;
import com.acleda.bsonlineshop.dto.revoke.RevokeRequestResponse;
import com.acleda.bsonlineshop.dto.revoke.RevokeReviewRequest;
import java.util.UUID;

public interface RevokeRequestService {
    PageResponse<RevokeRequestResponse> list(UUID shopId, String status, int page, int limit);

    RevokeRequestResponse create(UUID shopId, RevokeCreateRequest request);

    RevokeRequestResponse review(UUID id, RevokeReviewRequest request);
}
