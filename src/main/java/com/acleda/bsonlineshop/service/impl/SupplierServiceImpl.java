package com.acleda.bsonlineshop.service.impl;

import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.dto.supplier.SupplierCreateRequest;
import com.acleda.bsonlineshop.dto.supplier.SupplierResponse;
import com.acleda.bsonlineshop.dto.supplier.SupplierUpdateRequest;
import com.acleda.bsonlineshop.entity.Supplier;
import com.acleda.bsonlineshop.exception.ResourceNotFoundException;
import com.acleda.bsonlineshop.mapper.SupplierMapper;
import com.acleda.bsonlineshop.repository.SupplierRepository;
import com.acleda.bsonlineshop.service.SupplierService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SupplierResponse> list(String search, int page, int limit) {
        return PageResponse.from(supplierRepository
                .search(search, PageRequest.of(Math.max(page - 1, 0), limit))
                .map(supplierMapper::toResponse));
    }

    @Override
    @Transactional
    public SupplierResponse create(SupplierCreateRequest request) {
        Supplier supplier = new Supplier();
        supplierMapper.applyCreate(supplier, request);
        supplier.setJoinDate(LocalDate.now());
        supplier.setStatus("active");
        supplier.setTotalOrders(0);
        supplier.setTotalSpent(BigDecimal.ZERO);
        supplier.setRating(0);
        return supplierMapper.toResponse(supplierRepository.save(supplier));
    }

    @Override
    @Transactional
    public SupplierResponse update(UUID id, SupplierUpdateRequest request) {
        Supplier supplier = supplierRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));
        supplierMapper.applyUpdate(supplier, request);
        return supplierMapper.toResponse(supplierRepository.save(supplier));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Supplier supplier = supplierRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));
        supplier.setDeleted(true);
        supplierRepository.save(supplier);
    }
}
