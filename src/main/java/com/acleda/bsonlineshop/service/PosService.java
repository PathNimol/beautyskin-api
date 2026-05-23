package com.acleda.bsonlineshop.service;

import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.dto.pos.PosReceiptResponse;
import com.acleda.bsonlineshop.dto.pos.PosSaleRequest;
import java.util.UUID;

public interface PosService {
    PageResponse<PosReceiptResponse> listReceipts(UUID shopId, int page, int limit);

    PosReceiptResponse completeSale(UUID shopId, PosSaleRequest request);

    PosReceiptResponse cancelReceipt(UUID id, UUID shopId);
}
