package com.acleda.bsonlineshop.controller;

import com.acleda.bsonlineshop.dto.common.ApiResponse;
import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.dto.pos.PosReceiptResponse;
import com.acleda.bsonlineshop.dto.pos.PosSaleRequest;
import com.acleda.bsonlineshop.service.PosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pos")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("@authz.adminOrMerchant()")
public class PosController {

    private final PosService posService;

    @Operation(summary = "List POS receipts", description = "Recent in-store receipts for the last 30 days for a shop.")
    @GetMapping("/receipts")
    public ApiResponse<PageResponse<PosReceiptResponse>> receipts(
            @RequestParam UUID shopId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.success(posService.listReceipts(shopId, page, limit));
    }

    @Operation(summary = "Complete POS sale", description = "Record a walk-in sale; decrements product stock when productId is provided.")
    @PostMapping("/sales")
    public ApiResponse<PosReceiptResponse> completeSale(
            @RequestParam UUID shopId, @Valid @RequestBody PosSaleRequest request) {
        return ApiResponse.success("Sale completed", posService.completeSale(shopId, request));
    }

    @Operation(summary = "Cancel POS receipt", description = "Mark a receipt cancelled (daily cancellation cap per shop applies).")
    @PostMapping("/receipts/{id}/cancel")
    public ApiResponse<PosReceiptResponse> cancel(@PathVariable UUID id, @RequestParam UUID shopId) {
        return ApiResponse.success("Receipt cancelled", posService.cancelReceipt(id, shopId));
    }
}
