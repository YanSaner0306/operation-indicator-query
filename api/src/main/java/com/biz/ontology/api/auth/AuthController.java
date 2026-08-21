/**
 * 模块4-5：登录、刷新、注销和当前主体REST接口。
 * 功能：返回内存使用的Access Token，并通过HttpOnly Cookie管理Refresh Token。
 * 技术栈：Spring MVC、Spring Security、ResponseCookie和Bean Validation。
 */
package com.biz.ontology.api.auth;

import com.biz.ontology.api.auth.dto.CurrentPrincipalResponse;
import com.biz.ontology.api.auth.dto.LoginRequest;
import com.biz.ontology.api.auth.dto.TokenResponse;
import com.biz.ontology.api.common.R;
import com.biz.ontology.auth.token.AccessTokenClaims;
import com.biz.ontology.auth.token.AuthenticationService;
import com.biz.ontology.auth.token.SecurityTokenProperties;
import com.biz.ontology.common.exception.BusinessException;
import com.biz.ontology.common.exception.PlatformErrorCode;
import com.biz.ontology.common.security.BearerTokenAuthenticationFilter;
import com.biz.ontology.common.security.PlatformPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    public static final String REFRESH_COOKIE = "refresh_token";

    private final AuthenticationService authenticationService;
    private final SecurityTokenProperties properties;
    private final Set<String> allowedOrigins;

    public AuthController(
            AuthenticationService authenticationService,
            SecurityTokenProperties properties,
            @Value("${platform.cors.allowed-origins:http://localhost:5173}") String allowedOrigins
    ) {
        this.authenticationService = authenticationService;
        this.properties = properties;
        this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .collect(Collectors.toUnmodifiableSet());
    }

    @PostMapping("/login")
    public R<TokenResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        AuthenticationService.AuthenticationResult result = authenticationService.login(request, metadata(httpRequest));
        writeRefreshCookie(httpResponse, result.refreshToken());
        return R.ok(result.response());
    }

    @PostMapping("/refresh")
    public R<TokenResponse> refresh(
            @CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        requireTrustedOrigin(request);
        AuthenticationService.AuthenticationResult result = authenticationService.refresh(refreshToken, metadata(request));
        writeRefreshCookie(response, result.refreshToken());
        return R.ok(result.response());
    }

    @PostMapping("/logout")
    public R<Void> logout(
            @AuthenticationPrincipal PlatformPrincipal principal,
            @CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        requireTrustedOrigin(request);
        AccessTokenClaims claims = (AccessTokenClaims) request.getAttribute(
                BearerTokenAuthenticationFilter.ACCESS_CLAIMS_ATTRIBUTE
        );
        authenticationService.logout(principal, refreshToken, claims);
        clearRefreshCookie(response);
        return R.ok();
    }

    @GetMapping("/me")
    public R<CurrentPrincipalResponse> me(@AuthenticationPrincipal PlatformPrincipal principal) {
        return R.ok(authenticationService.current(principal));
    }

    private AuthenticationService.RequestMetadata metadata(HttpServletRequest request) {
        return new AuthenticationService.RequestMetadata(request.getRemoteAddr(), request.getHeader("User-Agent"));
    }

    private void requireTrustedOrigin(HttpServletRequest request) {
        String origin = request.getHeader("Origin");
        if (origin == null || !allowedOrigins.contains(origin)) {
            throw new BusinessException(PlatformErrorCode.AUTH_REQUEST_INVALID, "刷新或注销请求来源不受信任");
        }
    }

    private void writeRefreshCookie(HttpServletResponse response, String value) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE, value)
                .httpOnly(true)
                .secure(properties.isRefreshCookieSecure())
                .sameSite("Strict")
                .path("/api/v1/auth")
                .maxAge(Duration.ofDays(properties.getRefreshTokenDays()))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE, "")
                .httpOnly(true)
                .secure(properties.isRefreshCookieSecure())
                .sameSite("Strict")
                .path("/api/v1/auth")
                .maxAge(Duration.ZERO)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
