package com.acleda.bsonlineshop.mapper;

import com.acleda.bsonlineshop.dto.review.ReviewResponse;
import com.acleda.bsonlineshop.entity.Review;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public ReviewResponse toResponse(Review r) {
        List<String> photos = r.getPhotos() == null ? List.of() : List.copyOf(r.getPhotos());
        return ReviewResponse.builder()
                .id(r.getId())
                .productId(r.getProduct() != null ? r.getProduct().getId() : null)
                .customerId(r.getCustomerId())
                .customerName(r.getCustomerName())
                .customerAvatar(r.getCustomerAvatar())
                .customerAvatarAlt(r.getCustomerAvatarAlt())
                .rating(r.getRating())
                .title(r.getTitle())
                .body(r.getBody())
                .photos(photos)
                .verified(r.isVerified())
                .helpful(r.getHelpful())
                .skinType(r.getSkinType())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
