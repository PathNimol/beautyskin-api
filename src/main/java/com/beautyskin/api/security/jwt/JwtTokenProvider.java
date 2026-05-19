package com.beautyskin.api.security.jwt;

import com.beautyskin.api.config.JwtProperties;
import com.beautyskin.api.model.entity.User;
import com.beautyskin.api.model.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

  public static final String CLAIM_TYPE = "type";
  public static final String CLAIM_ROLE = "role";
  public static final String CLAIM_SHOP_ID = "shopId";
  public static final String TYPE_ACCESS = "access";
  public static final String TYPE_REFRESH = "refresh";

  private final JwtProperties properties;
  private final SecretKey secretKey;

  public JwtTokenProvider(JwtProperties properties) {
    this.properties = properties;
    this.secretKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
  }

  public String createAccessToken(User user) {
    return buildToken(user, TYPE_ACCESS, properties.accessExpirationMs());
  }

  public String createRefreshToken(User user) {
    return buildToken(user, TYPE_REFRESH, properties.refreshExpirationMs());
  }

  public long getAccessExpirationSeconds() {
    return properties.accessExpirationMs() / 1000;
  }

  public Claims parseClaims(String token) {
    return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
  }

  public boolean isAccessToken(Claims claims) {
    return TYPE_ACCESS.equals(claims.get(CLAIM_TYPE, String.class));
  }

  public boolean isRefreshToken(Claims claims) {
    return TYPE_REFRESH.equals(claims.get(CLAIM_TYPE, String.class));
  }

  public UUID getUserId(Claims claims) {
    return UUID.fromString(claims.getSubject());
  }

  public UserRole getRole(Claims claims) {
    return UserRole.valueOf(claims.get(CLAIM_ROLE, String.class));
  }

  public void validateToken(String token) {
    try {
      parseClaims(token);
    } catch (ExpiredJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
      throw new InvalidJwtException("Invalid or expired token", e);
    }
  }

  private String buildToken(User user, String type, long expirationMs) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + expirationMs);

    var builder =
        Jwts.builder()
            .subject(user.getId().toString())
            .claim(CLAIM_TYPE, type)
            .claim(CLAIM_ROLE, user.getRole().name())
            .issuedAt(now)
            .expiration(expiry);

    if (user.getShopId() != null) {
      builder.claim(CLAIM_SHOP_ID, user.getShopId());
    }

    return builder.signWith(secretKey).compact();
  }
}
