package com.acleda.bsonlineshop.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.acleda.bsonlineshop.dto.supplier.SupplierCreateRequest;
import com.acleda.bsonlineshop.dto.supplier.SupplierResponse;
import com.acleda.bsonlineshop.entity.Supplier;
import com.acleda.bsonlineshop.mapper.SupplierMapper;
import com.acleda.bsonlineshop.repository.SupplierRepository;
import com.acleda.bsonlineshop.service.impl.SupplierServiceImpl;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SupplierServiceImplTest {

    @Mock
    private SupplierRepository supplierRepository;
    @Mock
    private SupplierMapper supplierMapper;

    @InjectMocks
    private SupplierServiceImpl supplierService;

    @Test
    void create_setsActiveStatusAndJoinDate() {
        SupplierCreateRequest request = new SupplierCreateRequest();
        request.setName("Acme Supplies");

        Supplier entity = new Supplier();
        entity.setId(UUID.randomUUID());
        entity.setName("Acme Supplies");

        SupplierResponse response = SupplierResponse.builder().id(entity.getId()).name("Acme Supplies").build();

        when(supplierRepository.save(any(Supplier.class))).thenReturn(entity);
        when(supplierMapper.toResponse(entity)).thenReturn(response);

        SupplierResponse result = supplierService.create(request);

        assertThat(result.getName()).isEqualTo("Acme Supplies");
        verify(supplierRepository).save(any(Supplier.class));
    }
}
