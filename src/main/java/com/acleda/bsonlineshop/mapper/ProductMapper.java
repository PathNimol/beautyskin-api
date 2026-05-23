package com.acleda.bsonlineshop.mapper;

import com.acleda.bsonlineshop.dto.product.ImageDto;
import com.acleda.bsonlineshop.dto.product.ProductCreateRequest;
import com.acleda.bsonlineshop.dto.product.ProductResponse;
import com.acleda.bsonlineshop.entity.Product;
import com.acleda.bsonlineshop.entity.ProductImage;
import com.acleda.bsonlineshop.entity.Shop;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductResponse toResponse(Product p) {
        Shop shop = p.getShop();
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
                .images(copyImages(p.getImages()))
                .description(p.getDescription())
                .ingredients(copyStrings(p.getIngredients()))
                .howToUse(p.getHowToUse())
                .skinTypes(copyStrings(p.getSkinTypes()))
                .expiryDate(p.getExpiryDate())
                .sku(p.getSku())
                .shopId(shop != null ? shop.getId() : null)
                .shopName(shop != null ? shop.getName() : null)
                .status(p.getStatus())
                .tags(copyStrings(p.getTags()))
                .weight(p.getWeight())
                .origin(p.getOrigin())
                .visible(p.isVisible())
                .build();
    }

    /** Detach element collections so JSON serialization works with open-in-view disabled. */
    private static List<String> copyStrings(List<String> source) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }
        return List.copyOf(source);
    }

    private static List<ImageDto> copyImages(List<ProductImage> source) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }
        return source.stream().map(i -> new ImageDto(i.getSrc(), i.getAlt())).collect(Collectors.toCollection(ArrayList::new));
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
