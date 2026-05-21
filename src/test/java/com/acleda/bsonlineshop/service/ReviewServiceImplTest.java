package com.acleda.bsonlineshop.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.acleda.bsonlineshop.dto.review.ReviewCreateRequest;
import com.acleda.bsonlineshop.entity.Product;
import com.acleda.bsonlineshop.entity.Review;
import com.acleda.bsonlineshop.entity.User;
import com.acleda.bsonlineshop.mapper.ReviewMapper;
import com.acleda.bsonlineshop.repository.ProductRepository;
import com.acleda.bsonlineshop.repository.ReviewRepository;
import com.acleda.bsonlineshop.repository.UserRepository;
import com.acleda.bsonlineshop.security.SecurityUtils;
import com.acleda.bsonlineshop.security.UserPrincipal;
import com.acleda.bsonlineshop.service.impl.ReviewServiceImpl;
import com.acleda.bsonlineshop.enums.AccountStatus;
import com.acleda.bsonlineshop.enums.UserRole;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ReviewMapper reviewMapper;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private final UUID userId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();

    @BeforeEach
    void auth() {
        User authUser = new User();
        authUser.setId(userId);
        authUser.setEmail("buyer@test.com");
        authUser.setPasswordHash("pass");
        authUser.setRole(UserRole.CUSTOMER);
        authUser.setStatus(AccountStatus.ACTIVE);
        UserPrincipal principal = new UserPrincipal(authUser);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    @AfterEach
    void clearAuth() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createReview_recalculatesProductRating() {
        Product product = new Product();
        product.setId(productId);
        product.setReviewCount(0);
        product.setRating(0);

        User user = new User();
        user.setId(userId);
        user.setFirstName("Jane");
        user.setLastName("Doe");

        ReviewCreateRequest request = new ReviewCreateRequest();
        request.setRating(5);
        request.setTitle("Great");
        request.setBody("Love it");

        when(productRepository.findByIdAndDeletedFalse(productId)).thenReturn(Optional.of(product));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));
        when(reviewRepository.countByProduct_IdAndDeletedFalse(productId)).thenReturn(1L);
        when(reviewRepository.averageRatingByProduct(productId)).thenReturn(5.0);

        reviewService.create(productId, request);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertThat(captor.getValue().getReviewCount()).isEqualTo(1);
        assertThat(captor.getValue().getRating()).isEqualTo(5.0);
    }
}
