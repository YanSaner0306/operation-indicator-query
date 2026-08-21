/**
 * 模块4：用户认证与凭证生命周期服务。
 * 功能：处理登录失败锁定、Access/Refresh签发、Refresh轮换重放检测和注销吊销。
 * 技术栈：Spring事务、BCrypt、JJWT、SecureRandom和JPA。
 */
package com.biz.ontology.auth.token;

import com.biz.ontology.api.auth.dto.CurrentPrincipalResponse;
import com.biz.ontology.api.auth.dto.LoginRequest;
import com.biz.ontology.api.auth.dto.TokenResponse;
import com.biz.ontology.auth.identity.model.AuthStatus;
import com.biz.ontology.auth.identity.model.AuthUserEntity;
import com.biz.ontology.auth.identity.repository.AuthUserRepository;
import com.biz.ontology.auth.identity.service.UserPermissionQueryService;
import com.biz.ontology.auth.token.model.RefreshTokenEntity;
import com.biz.ontology.auth.token.model.RevokedAccessTokenEntity;
import com.biz.ontology.auth.token.model.TokenStatus;
import com.biz.ontology.auth.token.repository.RefreshTokenRepository;
import com.biz.ontology.auth.token.repository.RevokedAccessTokenRepository;
import com.biz.ontology.common.exception.BusinessException;
import com.biz.ontology.common.exception.PlatformErrorCode;
import com.biz.ontology.common.security.PlatformPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Set;
import java.util.UUID;

@Service
public class AuthenticationService {
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_MINUTES = 15;

    private final AuthUserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RevokedAccessTokenRepository revokedAccessTokenRepository;
    private final UserPermissionQueryService permissionQueryService;
    private final PasswordEncoder passwordEncoder;
    private final AccessTokenService accessTokenService;
    private final TokenHashService tokenHashService;
    private final SecurityTokenProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthenticationService(
            AuthUserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            RevokedAccessTokenRepository revokedAccessTokenRepository,
            UserPermissionQueryService permissionQueryService,
            PasswordEncoder passwordEncoder,
            AccessTokenService accessTokenService,
            TokenHashService tokenHashService,
            SecurityTokenProperties properties
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.revokedAccessTokenRepository = revokedAccessTokenRepository;
        this.permissionQueryService = permissionQueryService;
        this.passwordEncoder = passwordEncoder;
        this.accessTokenService = accessTokenService;
        this.tokenHashService = tokenHashService;
        this.properties = properties;
    }

    @Transactional(noRollbackFor = BusinessException.class)
    public AuthenticationResult login(LoginRequest request, RequestMetadata metadata) {
        AuthUserEntity user = userRepository.findByUsernameAndDeletedFlagFalse(request.username().trim().toLowerCase())
                .orElseThrow(() -> new BusinessException(PlatformErrorCode.AUTH_LOGIN_FAILED));
        requireEnabled(user);
        LocalDateTime now = LocalDateTime.now();
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(now)) {
            throw new BusinessException(PlatformErrorCode.AUTH_LOGIN_FAILED, "账号已临时锁定，请稍后再试");
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            int failedCount = user.getFailedLoginCount() + 1;
            user.setFailedLoginCount(failedCount);
            if (failedCount >= MAX_FAILED_ATTEMPTS) {
                user.setLockedUntil(now.plusMinutes(LOCK_MINUTES));
            }
            userRepository.saveAndFlush(user);
            throw new BusinessException(PlatformErrorCode.AUTH_LOGIN_FAILED);
        }
        user.setFailedLoginCount(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(now);
        AuthUserEntity savedUser = userRepository.saveAndFlush(user);
        return issuePair(savedUser, UUID.randomUUID().toString(), metadata);
    }

    @Transactional(noRollbackFor = BusinessException.class)
    public AuthenticationResult refresh(String rawRefreshToken, RequestMetadata metadata) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new BusinessException(PlatformErrorCode.AUTH_TOKEN_INVALID);
        }
        RefreshTokenEntity current = refreshTokenRepository.findByTokenHash(tokenHashService.sha256(rawRefreshToken))
                .orElseThrow(() -> new BusinessException(PlatformErrorCode.AUTH_TOKEN_INVALID));
        LocalDateTime now = LocalDateTime.now();
        if (current.getStatus() != TokenStatus.ACTIVE) {
            refreshTokenRepository.revokeActiveFamily(current.getFamilyId(), TokenStatus.REVOKED, now);
            userRepository.findByIdAndDeletedFlagFalse(current.getUserId()).ifPresent(user -> {
                user.setTokenVersion(user.getTokenVersion() + 1);
                userRepository.save(user);
            });
            throw new BusinessException(PlatformErrorCode.AUTH_REFRESH_REPLAYED);
        }
        if (!current.getExpiresAt().isAfter(now)) {
            current.setStatus(TokenStatus.EXPIRED);
            current.setRevokedAt(now);
            refreshTokenRepository.save(current);
            throw new BusinessException(PlatformErrorCode.AUTH_TOKEN_EXPIRED);
        }
        AuthUserEntity user = userRepository.findByIdAndDeletedFlagFalse(current.getUserId())
                .orElseThrow(() -> new BusinessException(PlatformErrorCode.AUTH_TOKEN_INVALID));
        requireEnabled(user);
        AuthenticationResult result = issuePair(user, current.getFamilyId(), metadata);
        RefreshTokenEntity rotated = refreshTokenRepository.findByTokenHash(tokenHashService.sha256(result.refreshToken()))
                .orElseThrow();
        current.setStatus(TokenStatus.REVOKED);
        current.setRevokedAt(now);
        current.setRotatedToId(rotated.getId());
        refreshTokenRepository.save(current);
        return result;
    }

    @Transactional
    public void logout(PlatformPrincipal principal, String rawRefreshToken, AccessTokenClaims accessTokenClaims) {
        LocalDateTime now = LocalDateTime.now();
        if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
            refreshTokenRepository.findByTokenHash(tokenHashService.sha256(rawRefreshToken)).ifPresent(token -> {
                if (token.getStatus() == TokenStatus.ACTIVE) {
                    token.setStatus(TokenStatus.REVOKED);
                    token.setRevokedAt(now);
                    refreshTokenRepository.save(token);
                }
            });
        }
        if (accessTokenClaims != null && !revokedAccessTokenRepository.existsByJti(accessTokenClaims.jti())) {
            RevokedAccessTokenEntity revoked = new RevokedAccessTokenEntity();
            revoked.setJti(accessTokenClaims.jti());
            revoked.setUserId(Long.valueOf(principal.principalId()));
            revoked.setExpiresAt(LocalDateTime.ofInstant(accessTokenClaims.expiresAt(), ZoneOffset.UTC));
            revoked.setCreatedAt(now);
            revokedAccessTokenRepository.save(revoked);
        }
    }

    @Transactional(readOnly = true)
    public CurrentPrincipalResponse current(PlatformPrincipal principal) {
        AuthUserEntity user = userRepository.findByIdAndDeletedFlagFalse(Long.valueOf(principal.principalId()))
                .orElseThrow(() -> new BusinessException(PlatformErrorCode.AUTH_TOKEN_INVALID));
        requireEnabled(user);
        Set<String> permissions = permissionQueryService.findEffectivePermissions(user.getId());
        return new CurrentPrincipalResponse(user.getId(), user.getUsername(), user.getDisplayName(), permissions);
    }

    private AuthenticationResult issuePair(AuthUserEntity user, String familyId, RequestMetadata metadata) {
        AccessTokenService.IssuedAccessToken accessToken = accessTokenService.issue(user);
        String rawRefreshToken = generateRefreshToken();
        RefreshTokenEntity refreshToken = new RefreshTokenEntity();
        refreshToken.setUserId(user.getId());
        refreshToken.setTokenHash(tokenHashService.sha256(rawRefreshToken));
        refreshToken.setJti(UUID.randomUUID().toString());
        refreshToken.setFamilyId(familyId);
        refreshToken.setStatus(TokenStatus.ACTIVE);
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(properties.getRefreshTokenDays()));
        refreshToken.setClientIp(limit(metadata.clientIp(), 64));
        refreshToken.setUserAgent(limit(metadata.userAgent(), 500));
        refreshToken.setCreatedAt(LocalDateTime.now());
        refreshTokenRepository.saveAndFlush(refreshToken);
        Set<String> permissions = permissionQueryService.findEffectivePermissions(user.getId());
        CurrentPrincipalResponse principal = new CurrentPrincipalResponse(
                user.getId(), user.getUsername(), user.getDisplayName(), permissions
        );
        TokenResponse response = new TokenResponse(
                accessToken.token(), "Bearer", accessTokenService.accessTokenSeconds(), principal
        );
        return new AuthenticationResult(response, rawRefreshToken);
    }

    private void requireEnabled(AuthUserEntity user) {
        if (user.getStatus() != AuthStatus.ENABLED) {
            throw new BusinessException(PlatformErrorCode.AUTH_PRINCIPAL_DISABLED);
        }
    }

    private String generateRefreshToken() {
        byte[] value = new byte[32];
        secureRandom.nextBytes(value);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    private String limit(String value, int max) {
        if (value == null) return null;
        return value.length() <= max ? value : value.substring(0, max);
    }

    public record AuthenticationResult(TokenResponse response, String refreshToken) {
    }

    public record RequestMetadata(String clientIp, String userAgent) {
    }
}
