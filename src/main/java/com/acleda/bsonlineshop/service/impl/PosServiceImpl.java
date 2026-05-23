package com.acleda.bsonlineshop.service.impl;

import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.dto.pos.PosReceiptResponse;
import com.acleda.bsonlineshop.dto.pos.PosSaleLineRequest;
import com.acleda.bsonlineshop.dto.pos.PosSaleRequest;
import com.acleda.bsonlineshop.entity.PosReceipt;
import com.acleda.bsonlineshop.entity.PosReceiptLine;
import com.acleda.bsonlineshop.entity.Shop;
import com.acleda.bsonlineshop.exception.BusinessException;
import com.acleda.bsonlineshop.exception.ResourceNotFoundException;
import com.acleda.bsonlineshop.mapper.PosMapper;
import com.acleda.bsonlineshop.repository.PosReceiptRepository;
import com.acleda.bsonlineshop.repository.ProductRepository;
import com.acleda.bsonlineshop.repository.ShopRepository;
import com.acleda.bsonlineshop.security.SecurityUtils;
import com.acleda.bsonlineshop.service.PosService;
import com.acleda.bsonlineshop.service.StockEventHelper;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PosServiceImpl implements PosService {

    private final PosReceiptRepository posReceiptRepository;
    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;
    private final PosMapper posMapper;
    private final StockEventHelper stockEventHelper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PosReceiptResponse> listReceipts(UUID shopId, int page, int limit) {
        UUID scoped = SecurityUtils.requireShopId(shopId);
        Instant since = Instant.now().minus(30, ChronoUnit.DAYS);
        return PageResponse.from(posReceiptRepository
                .findRecentByShop(scoped, since, PageRequest.of(Math.max(page - 1, 0), limit))
                .map(posMapper::toResponse));
    }

    @Override
    @Transactional
    public PosReceiptResponse completeSale(UUID shopId, PosSaleRequest request) {
        Shop shop = shopRepository.findByIdAndDeletedFalse(SecurityUtils.requireShopId(shopId))
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));
        PosReceipt receipt = new PosReceipt();
        receipt.setReceiptRef("RCP-" + System.currentTimeMillis());
        receipt.setShop(shop);
        receipt.setCustomerName(request.getCustomerName() != null ? request.getCustomerName() : "Walk-in");
        receipt.setCustomerPhone(request.getCustomerPhone() != null ? request.getCustomerPhone() : "");
        receipt.setPaymentMethod(request.getPaymentMethod());
        BigDecimal subtotal = BigDecimal.ZERO;
        for (PosSaleLineRequest line : request.getItems()) {
            PosReceiptLine rl = new PosReceiptLine();
            rl.setReceipt(receipt);
            rl.setProductId(line.getProductId());
            rl.setProductName(line.getName());
            rl.setQuantity(line.getQuantity());
            rl.setUnitPrice(line.getPrice());
            rl.setLineTotal(line.getPrice().multiply(BigDecimal.valueOf(line.getQuantity())));
            receipt.getLines().add(rl);
            subtotal = subtotal.add(rl.getLineTotal());
            decrementProductStock(line);
        }
        BigDecimal discount = request.getDiscount() != null ? request.getDiscount() : BigDecimal.ZERO;
        BigDecimal taxRate = new BigDecimal("0.10");
        BigDecimal taxable = subtotal.subtract(discount);
        BigDecimal tax = taxable.multiply(taxRate);
        receipt.setSubtotal(subtotal);
        receipt.setDiscount(discount);
        receipt.setTax(tax);
        receipt.setTotal(taxable.add(tax));
        receipt.setCancelled(false);
        return posMapper.toResponse(posReceiptRepository.save(receipt));
    }

    @Override
    @Transactional
    public PosReceiptResponse cancelReceipt(UUID id, UUID shopId) {
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
        return posMapper.toResponse(posReceiptRepository.save(receipt));
    }

    private void decrementProductStock(PosSaleLineRequest line) {
        if (line.getProductId() == null) return;
        productRepository.findByIdAndDeletedFalseForUpdate(line.getProductId()).ifPresent(product -> {
            int newStock = Math.max(0, product.getStock() - line.getQuantity());
            product.setStock(newStock);
            product.setSold(product.getSold() + line.getQuantity());
            productRepository.save(product);
            stockEventHelper.record(product, -line.getQuantity(), "pos_sale");
        });
    }
}
