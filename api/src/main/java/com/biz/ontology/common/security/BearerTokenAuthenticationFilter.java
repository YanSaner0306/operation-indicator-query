/**
 * 模块4-5：Bearer JWT认证过滤器。
 * 功能：校验JWT、用户状态、tokenVersion、jti吊销状态，并加载数据库中的实时权限。
 * 技术栈：Spring Security OncePerRequestFilter、JJWT和Spring Data JPA。
 */
package com.biz.ontology.common.security;

import com.biz.ontology.api.common.R;
import com.biz.ontology.auth.identity.model.AuthStatus;
import com.biz.ontology.auth.identity.model.AuthUserEntity;
import com.biz.ontology.auth.identity.repository.AuthUserRepository;
import com.biz.ontology.auth.identity.service.UserPermissionQueryService;
import com.biz.ontology.auth.token.AccessTokenClaims;
import com.biz.ontology.auth.token.AccessTokenService;
import com.biz.ontology.auth.token.repository.RevokedAccessTokenRepository;
import com.biz.ontology.common.exception.BusinessException;
import com.biz.ontology.common.exception.PlatformErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
public class BearerTokenAuthenticationFilter extends OncePerRequestFilter {
    public static final String ACCESS_CLAIMS_ATTRIBUTE = "platform.accessTokenClaims";

    private final AccessTokenService accessTokenService;
    private final AuthUserRepository userRepository;
    private final UserPermissionQueryService permissionQueryService;
    private final RevokedAccessTokenRepository revokedAccessTokenRepository;
    private final ObjectMapper objectMapper;

    public BearerTokenAuthenticationFilter(
            AccessTokenService accessTokenService,
            AuthUserRepository userRepository,
            UserPermissionQueryService permissionQueryService,
            RevokedAccessTokenRepository revokedAccessTokenRepository,
            ObjectMapper objectMapper
    ) {
        this.accessTokenService = accessTokenService;
        this.userRepository = userRepository;
        this.permissionQueryService = permissionQueryService;
        this.revokedAccessTokenRepository = revokedAccessTokenRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        String apiKey = request.getHeader("X-API-Key");
        if (authorization != null && !authorization.isBlank() && apiKey != null && !apiKey.isBlank()) {
            writeFailure(response, PlatformErrorCode.AUTH_MULTIPLE_CREDENTIALS);
            return;
        }
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            AccessTokenClaims claims = accessTokenService.parse(authorization.substring(7).trim());
            AuthUserEntity user = userRepository.findByIdAndDeletedFlagFalse(claims.userId())
                    .orElseThrow(() -> new BusinessException(PlatformErrorCode.AUTH_TOKEN_INVALID));
            if (user.getStatus() != AuthStatus.ENABLED) {
                throw new BusinessException(PlatformErrorCode.AUTH_PRINCIPAL_DISABLED);
            }
            if (!user.getTokenVersion().equals(claims.tokenVersion()) || revokedAccessTokenRepository.existsByJti(claims.jti())) {
                throw new BusinessException(PlatformErrorCode.AUTH_TOKEN_INVALID);
            }
            Set<String> permissions = permissionQueryService.findEffectivePermissions(user.getId());
            PlatformPrincipal principal = new PlatformPrincipal(
                    String.valueOf(user.getId()), PlatformPrincipal.PrincipalType.USER,
                    user.getDisplayName(), permissions, claims.jti(), claims.tokenVersion()
            );
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    permissions.stream().map(SimpleGrantedAuthority::new).toList()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            request.setAttribute(ACCESS_CLAIMS_ATTRIBUTE, claims);
            filterChain.doFilter(request, response);
        } catch (BusinessException exception) {
            SecurityContextHolder.clearContext();
            writeFailure(response, exception.getErrorCode());
        }
    }

    private void writeFailure(HttpServletResponse response, PlatformErrorCode code) throws IOException {
        response.setStatus(code.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), R.error(code.getResponseCode(), code.getDefaultMessage()));
    }
}
