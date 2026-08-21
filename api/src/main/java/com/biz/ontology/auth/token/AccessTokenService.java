/**
 * 模块4：JWT Access Token服务。
 * 功能：使用HS256签发最小声明JWT，并区分过期、格式和签名错误。
 * 技术栈：JJWT 0.12、Java时间API与HMAC-SHA-256。
 */
package com.biz.ontology.auth.token;

import com.biz.ontology.auth.identity.model.AuthUserEntity;
import com.biz.ontology.common.exception.BusinessException;
import com.biz.ontology.common.exception.PlatformErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
public class AccessTokenService {
    private final SecurityTokenProperties properties;
    private SecretKey key;

    public AccessTokenService(SecurityTokenProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    void initializeKey() {
        try {
            byte[] decoded = Decoders.BASE64.decode(properties.getJwtSecret());
            if (decoded.length < 32) {
                throw new IllegalStateException("JWT密钥至少需要256 bit");
            }
            key = Keys.hmacShaKeyFor(decoded);
        } catch (RuntimeException exception) {
            throw new IllegalStateException("platform.security.jwt-secret必须是有效Base64密钥", exception);
        }
    }

    public IssuedAccessToken issue(AuthUserEntity user) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(properties.getAccessTokenMinutes(), ChronoUnit.MINUTES);
        String jti = UUID.randomUUID().toString();
        String token = Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .id(jti)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .claim("username", user.getUsername())
                .claim("tokenVersion", user.getTokenVersion())
                .signWith(key)
                .compact();
        return new IssuedAccessToken(token, jti, expiresAt);
    }

    public AccessTokenClaims parse(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            return new AccessTokenClaims(
                    Long.valueOf(claims.getSubject()),
                    claims.get("username", String.class),
                    claims.getId(),
                    claims.get("tokenVersion", Long.class),
                    claims.getExpiration().toInstant()
            );
        } catch (ExpiredJwtException exception) {
            throw new BusinessException(PlatformErrorCode.AUTH_TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException exception) {
            throw new BusinessException(PlatformErrorCode.AUTH_TOKEN_INVALID);
        }
    }

    public long accessTokenSeconds() {
        return properties.getAccessTokenMinutes() * 60;
    }

    public record IssuedAccessToken(String token, String jti, Instant expiresAt) {
    }
}
