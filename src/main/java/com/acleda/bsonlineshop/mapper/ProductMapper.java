package com.acleda.bsonlineshop.mapper;

import com.acleda.bsonlineshop.dto.product.ImageDto;
import com.acleda.bsonlineshop.dto.product.ProductCreateRequest;
import com.acleda.bsonlineshop.dto.product.ProductResponse;
import com.acleda.bsonlineshop.entity.Product;
import com.acleda.bsonlineshop.entity.ProductImage;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductResponse toResponse(Product p) {
        return ProductResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .brand(p.getBrand())
                .category(p.getCategory())
                .price(p.getPrice())
                .originalPrice(p.getOriginalPrice())
                .stock(p.getStock())
                .sold(p.getSold())
                .rating(p.getRating())
                .reviewCount(p.getReviewCount())
                .image(p.getImage())
                .imageAlt(p.getImageAlt())
                .images(p.getImages().stream().map(i -> new ImageDto(i.getSrc(), i.getAlt())).collect(Collectors.toList()))
                .description(p.getDescription())
                .ingredients(p.getIngredients())
                .howToUse(p.getHowToUse())
                .skinTypes(p.getSkinTypes())
                .expiryDate(p.getExpiryDate())
                .sku(p.getSku())
                .shopId(p.getShop() != null ? p.getShop().getId() : null)
                .shopName(p.getShop() != null ? p.getShop().getName() : null)
                .status(p.getStatus())
                .tags(p.getTags())
                .weight(p.getWeight())
                .origin(p.getOrigin())
                .visible(p.isVisible())
                .build();
    }

    public void applyCreate(Product p, ProductCreateRequest req) {
        p.setName(req.getName());
        p.setBrand(req.getBrand());
        p.setCategory(req.getCategory());
        p.setPrice(req.getPrice());
        p.setOriginalPrice(req.getOriginalPrice());
        p.setStock(req.getStock());
        p.setImage(req.getImage());
        p.setImageAlt(req.getImageAlt());
        if (req.getImages() != null) {
            p.setImages(req.getImages().stream()
                    .map(i -> new ProductImage(i.getSrc(), i.getAlt()))
                    .collect(Collectors.toList()));
        }
        p.setDescription(req.getDescription());
        p.setIngredients(req.getIngredients());
        p.setHowToUse(req.getHowToUse());
        p.setSkinTypes(req.getSkinTypes());
        p.setExpiryDate(req.getExpiryDate());
        p.setSku(req.getSku());
        if (req.getStatus() != null) p.setStatus(req.getStatus());
        p.setTags(req.getTags());
        p.setWeight(req.getWeight());
        p.setOrigin(req.getOrigin());
        p.setVisible(req.isVisible());
    }
}
