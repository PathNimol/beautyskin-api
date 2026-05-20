package com.acleda.bsonlineshop.controller;

import com.acleda.bsonlineshop.dto.common.ApiResponse;
import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.entity.PosReceipt;
import com.acleda.bsonlineshop.entity.PosReceiptLine;
import com.acleda.bsonlineshop.entity.Shop;
import com.acleda.bsonlineshop.exception.BusinessException;
import com.acleda.bsonlineshop.exception.ResourceNotFoundException;
import com.acleda.bsonlineshop.repository.PosReceiptRepository;
import com.acleda.bsonlineshop.repository.ShopRepository;
import com.acleda.bsonlineshop.security.SecurityUtils;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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

    private final PosReceiptRepository posReceiptRepository;
    private final ShopRepository shopRepository;

    @Operation(summary = "List POS receipts", description = "Recent in-store receipts for the last 30 days for a shop.")
    @GetMapping("/receipts")
    public ApiResponse<PageResponse<PosReceipt>> receipts(
            @RequestParam UUID shopId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        UUID scoped = SecurityUtils.requireShopId(shopId);
        Instant since = Instant.now().minus(30, ChronoUnit.DAYS);
        return ApiResponse.success(PageResponse.from(
                posReceiptRepository.findRecentByShop(scoped, since, PageRequest.of(Math.max(page - 1, 0), limit))));
    }

    @Operation(summary = "Complete POS sale", description = "Record a walk-in sale with line items, optional discount, tax calculation, and payment method.")
    @PostMapping("/sales")
    public ApiResponse<PosReceipt> completeSale(@RequestParam UUID shopId, @RequestBody Map<String, Object> body) {
        Shop shop = shopRepository.findByIdAndDeletedFalse(SecurityUtils.requireShopId(shopId))
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));
        PosReceipt receipt = new PosReceipt();
        receipt.setReceiptRef("RCP-" + System.currentTimeMillis());
        receipt.setShop(shop);
        receipt.setCustomerName((String) body.getOrDefault("customerName", "Walk-in"));
        receipt.setCustomerPhone((String) body.getOrDefault("customerPhone", ""));
        receipt.setPaymentMethod((String) body.get("paymentMethod"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> lines = (List<Map<String, Object>>) body.get("items");
        BigDecimal subtotal = BigDecimal.ZERO;
        for (Map<String, Object> line : lines) {
            PosReceiptLine rl = new PosReceiptLine();
            rl.setReceipt(receipt);
            rl.setProductName((String) line.get("name"));
            rl.setQuantity((Integer) line.get("quantity"));
            rl.setUnitPrice(new BigDecimal(line.get("price").toString()));
            rl.setLineTotal(rl.getUnitPrice().multiply(BigDecimal.valueOf(rl.getQuantity())));
            receipt.getLines().add(rl);
            subtotal = subtotal.add(rl.getLineTotal());
        }
        BigDecimal discount = body.get("discount") != null
                ? new BigDecimal(body.get("discount").toString()) : BigDecimal.ZERO;
        BigDecimal taxRate = new BigDecimal("0.10");
        BigDecimal taxable = subtotal.subtract(discount);
        BigDecimal tax = taxable.multiply(taxRate);
        receipt.setSubtotal(subtotal);
        receipt.setDiscount(discount);
        receipt.setTax(tax);
        receipt.setTotal(taxable.add(tax));
        receipt.setCancelled(false);
        return ApiResponse.success("Sale completed", posReceiptRepository.save(receipt));
    }

    @Operation(summary = "Cancel POS receipt", description = "Mark a receipt cancelled (daily cancellation cap per shop applies).")
    @PostMapping("/receipts/{id}/cancel")
    public ApiResponse<PosReceipt> cancel(@PathVariable UUID id, @RequestParam UUID shopId) {
        UUID scoped = SecurityUtils.requireShopId(shopId);
        Instant startOfDay = Instant.now().truncatedTo(ChronoUnit.DAYS);
        long cancellations = posReceiptRepository.countByShop_IdAndCancelledTrueAndCreatedAtAfterAndDeletedFalse(
                scoped, startOfDay);
        if (cancellations >= 3) {
            throw new BusinessException("Daily cancellation limit reached");
        }
        PosReceipt receipt = posReceiptRepository.findById(id).filter(r -> !r.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Receipt not found"));
        receipt.setCancelled(true);
        return ApiResponse.success("Receipt cancelled", posReceiptRepository.save(receipt));
    }
}
