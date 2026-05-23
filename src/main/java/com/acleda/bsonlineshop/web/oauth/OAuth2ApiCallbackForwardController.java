package com.acleda.bsonlineshop.web.oauth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Google OAuth redirect URI is registered as {@code /api/login/oauth2/code/google}
 * while Spring Security's {@code OAuth2LoginAuthenticationFilter} listens on
 * {@code /login/oauth2/code/*}. This forwards internally so the code exchange runs.
 */
@Controller
@Slf4j
public class OAuth2ApiCallbackForwardController {

    @GetMapping("/api/login/oauth2/code/{registrationId}")
    public void forwardToSpringOAuthCallback(
            @PathVariable String registrationId,
            HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        String query = request.getQueryString();
        String target = "/login/oauth2/code/" + registrationId;
        if (query != null && !query.isBlank()) {
            target += "?" + query;
        }
        log.debug("OAuth callback forward: {} -> {}", request.getRequestURI(), target);
        request.getRequestDispatcher(target).forward(request, response);
    }
}
