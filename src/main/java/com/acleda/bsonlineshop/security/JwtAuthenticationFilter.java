package com.acleda.bsonlineshop.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/oauth2/")
                || path.startsWith("/login/oauth2/")
                || path.startsWith("/api/login/oauth2/")
                || path.startsWith("/api/oauth2/");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        String jwt = resolveJwt(request);
        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            String username = jwtService.extractUsername(jwt);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserPrincipal principal = (UserPrincipal) userDetailsService.loadUserByUsername(username);
                if (principal.isEnabled()
                        && principal.isAccountNonLocked()
                        && jwtService.isTokenValid(jwt, principal)) {
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        } catch (UsernameNotFoundException ignored) {
            // Invalid or deleted user — leave context unauthenticated
        } catch (JwtException ignored) {
            // Expired, malformed, or invalid signature — treat as no auth (re-login in Swagger/UI)
        }
        filterChain.doFilter(request, response);
    }

    /** Bearer header, or {@code access_token} / {@code token} query param (for EventSource SSE). */
    private static String resolveJwt(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        String query = request.getParameter("access_token");
        if (query == null || query.isBlank()) {
            query = request.getParameter("token");
        }
        return query != null && !query.isBlank() ? query : null;
    }
}
