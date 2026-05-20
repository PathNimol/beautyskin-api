package com.acleda.bsonlineshop.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.access-expiration-ms}")
    private long accessExpirationMs;

    public String generateAccessToken(UserPrincipal principal) {
        return buildToken(Map.of(
                "role", principal.getRole().name(),
                "shopId", principal.getShopId() != null ? principal.getShopId().toString() : ""),
                principal.getEmail(),
                accessExpirationMs);
    }

    public String generateRefreshTokenValue() {
        return UUID.randomUUID().toString() + "." + UUID.randomUUID();
    }

    private String buildToken(Map<String, Object> extraClaims, String subject, long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignKey())
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserPrincipal principal) {
        String username = extractUsername(token);
        return username.equals(principal.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return resolver.apply(claims);
    }

    private SecretKey getSignKey() {
        byte[] keyBytes = secret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            keyBytes = padKey(secret);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private byte[] padKey(String value) {
        byte[] bytes = new byte[32];
        byte[] src = value.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        System.arraycopy(src, 0, bytes, 0, Math.min(src.length, 32));
        return bytes;
    }
}
