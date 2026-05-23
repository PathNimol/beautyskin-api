package com.acleda.bsonlineshop.security.oauth2;

import com.acleda.bsonlineshop.entity.RefreshToken;
import com.acleda.bsonlineshop.entity.User;
import com.acleda.bsonlineshop.enums.AccountStatus;
import com.acleda.bsonlineshop.enums.UserRole;
import com.acleda.bsonlineshop.exception.BusinessException;
import com.acleda.bsonlineshop.repository.RefreshTokenRepository;
import com.acleda.bsonlineshop.repository.UserRepository;
import com.acleda.bsonlineshop.security.JwtService;
import com.acleda.bsonlineshop.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    @Value("${app.oauth2.frontend-redirect-url}")
    private String frontendRedirectUrl;

    @Value("${app.jwt.access-expiration-ms}")
    private long accessExpirationMs;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = token.getPrincipal();
        String provider = token.getAuthorizedClientRegistrationId();

        String email = OAuth2UserInfoFactory.getEmail(provider, oAuth2User);
        String firstName = OAuth2UserInfoFactory.getFirstName(provider, oAuth2User);
        String lastName = OAuth2UserInfoFactory.getLastName(provider, oAuth2User);
        String avatar = OAuth2UserInfoFactory.getAvatar(provider, oAuth2User);

        if (email == null || email.isBlank()) {
            throw new BusinessException("Google account did not provide an email address");
        }

        User user = userRepository.findByEmailIgnoreCaseAndDeletedFalse(email)
                .orElseGet(() -> {
                    User u = new User();
                    u.setEmail(email);
                    u.setPasswordHash("oauth2-no-password");
                    u.setFirstName(firstName);
                    u.setLastName(lastName);
                    u.setAvatar(avatar);
                    u.setAvatarAlt(firstName + " " + lastName);
                    u.setRole(UserRole.CUSTOMER);
                    u.setStatus(AccountStatus.ACTIVE);
                    u.setJoinDate(Instant.now());
                    u.setEmailVerified(true);
                    return userRepository.save(u);
                });

        if (user.getStatus() != AccountStatus.ACTIVE) {
            String msg = URLEncoder.encode("Account is not active. Use email/password login.", StandardCharsets.UTF_8);
            getRedirectStrategy().sendRedirect(request, response, frontendRedirectUrl + "?error=" + msg);
            return;
        }

        if (avatar != null && !avatar.equals(user.getAvatar())) {
            user.setAvatar(avatar);
            userRepository.save(user);
        }

        UserPrincipal principal = UserPrincipal.from(user);
        String accessToken = jwtService.generateAccessToken(principal);
        String refreshValue = jwtService.generateRefreshTokenValue();
        RefreshToken refresh = new RefreshToken();
        refresh.setUser(user);
        refresh.setToken(refreshValue);
        refresh.setExpiresAt(Instant.now().plusMillis(refreshExpirationMs));
        refresh.setRevoked(false);
        refreshTokenRepository.save(refresh);

        String redirectUrl = UriComponentsBuilder.fromUriString(frontendRedirectUrl)
                .queryParam("access_token", accessToken)
                .queryParam("refresh_token", refreshValue)
                .queryParam("expires_in", accessExpirationMs / 1000)
                .build()
                .encode()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
