package com.acleda.bsonlineshop.service.impl;

import com.acleda.bsonlineshop.dto.common.ListRequest;
import com.acleda.bsonlineshop.dto.common.PageAbleResponse;
import com.acleda.bsonlineshop.dto.common.PageResponse;
import com.acleda.bsonlineshop.dto.common.Result;
import com.acleda.bsonlineshop.dto.product.ProductCreateRequest;
import com.acleda.bsonlineshop.dto.product.ProductResponse;
import com.acleda.bsonlineshop.entity.Product;
import com.acleda.bsonlineshop.entity.Shop;
import com.acleda.bsonlineshop.enums.ProductStatus;
import com.acleda.bsonlineshop.exception.ResourceNotFoundException;
import com.acleda.bsonlineshop.mapper.ProductMapper;
import com.acleda.bsonlineshop.repository.ProductRepository;
import com.acleda.bsonlineshop.repository.ShopRepository;
import com.acleda.bsonlineshop.security.SecurityUtils;
import com.acleda.bsonlineshop.service.ProductService;
import com.acleda.bsonlineshop.utils.CoreBase;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional(readOnly = true)
    public Result<Object> listCatalog(ListRequest request) {
        Sort sort = Sort.by(
                Sort.Direction.fromString(request.getSortDirection()),
                request.getSortProperty()
        );
        Pageable pageable = PageRequest.of(request.getPageNumber(), request.getSize(), sort);
        Specification<Product> spec = CoreBase.filter(request, Product.class);
        Page<Product> products = productRepository.findAll(spec, pageable);
        List<ProductResponse> responses = products.stream()
                .map(productMapper::toResponse)
                .toList();
        return Result.of(new PageAbleResponse<>(responses, products));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(UUID id) {
        Product p = productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return productMapper.toResponse(p);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> listMerchant(
            UUID shopId, String search, String category, String status, int page, int limit) {
        UUID scopedShop = SecurityUtils.requireShopId(shopId);
        ProductStatus productStatus = status != null && !status.isBlank()
                ? ProductStatus.valueOf(status.toUpperCase().replace(' ', '_'))
                : null;

        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("deleted")));
            predicates.add(cb.equal(root.get("shop").get("id"), scopedShop));
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("brand")), pattern)
                ));
            }
            if (category != null && !category.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("category")), category.toLowerCase()));
            }
            if (productStatus != null) {
                predicates.add(cb.equal(root.get("status"), productStatus));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), limit, Sort.by("name"));
        Page<Product> result = productRepository.findAll(spec, pageable);
        return PageResponse.from(result.map(productMapper::toResponse));
    }

    @Override
    @Transactional
    public ProductResponse create(UUID shopId, ProductCreateRequest request) {
        Shop shop = shopRepository.findByIdAndDeletedFalse(SecurityUtils.requireShopId(shopId))
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));
        Product p = new Product();
        p.setShop(shop);
        productMapper.applyCreate(p, request);
        return productMapper.toResponse(productRepository.save(p));
    }

    @Override
    @Transactional
    public ProductResponse update(UUID shopId, UUID productId, ProductCreateRequest request) {
        Product p = findShopProduct(SecurityUtils.requireShopId(shopId), productId);
        productMapper.applyCreate(p, request);
        return productMapper.toResponse(productRepository.save(p));
    }

    @Override
    @Transactional
    public void delete(UUID shopId, UUID productId) {
        Product p = findShopProduct(SecurityUtils.requireShopId(shopId), productId);
        p.setDeleted(true);
        productRepository.save(p);
    }

    private Product findShopProduct(UUID shopId, UUID productId) {
        Product p = productRepository.findByIdAndDeletedFalse(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        if (!p.getShop().getId().equals(shopId)) {
            throw new ResourceNotFoundException("Product not found in shop");
        }
        return p;
    }
}