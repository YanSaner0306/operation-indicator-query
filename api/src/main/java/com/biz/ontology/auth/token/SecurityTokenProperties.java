/**
 * 模块4：认证令牌配置模型。
 * 功能：集中读取JWT密钥、Access/Refresh有效期和Cookie安全开关。
 * 技术栈：Spring Boot ConfigurationProperties外部化配置。
 */
package com.biz.ontology.auth.token;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "platform.security")
public class SecurityTokenProperties {
    private String jwtSecret;
    private long accessTokenMinutes = 30;
    private long refreshTokenDays = 7;
    private boolean refreshCookieSecure = true;
    private String bootstrapAdminUsername = "admin";
    private String bootstrapAdminPassword;
    private String apiKeyPepper;
    private long apiKeyDefaultDays = 180;

    public String getJwtSecret() { return jwtSecret; }
    public void setJwtSecret(String jwtSecret) { this.jwtSecret = jwtSecret; }
    public long getAccessTokenMinutes() { return accessTokenMinutes; }
    public void setAccessTokenMinutes(long accessTokenMinutes) { this.accessTokenMinutes = accessTokenMinutes; }
    public long getRefreshTokenDays() { return refreshTokenDays; }
    public void setRefreshTokenDays(long refreshTokenDays) { this.refreshTokenDays = refreshTokenDays; }
    public boolean isRefreshCookieSecure() { return refreshCookieSecure; }
    public void setRefreshCookieSecure(boolean refreshCookieSecure) { this.refreshCookieSecure = refreshCookieSecure; }
    public String getBootstrapAdminUsername() { return bootstrapAdminUsername; }
    public void setBootstrapAdminUsername(String value) { bootstrapAdminUsername = value; }
    public String getBootstrapAdminPassword() { return bootstrapAdminPassword; }
    public void setBootstrapAdminPassword(String value) { bootstrapAdminPassword = value; }
    public String getApiKeyPepper() { return apiKeyPepper; }
    public void setApiKeyPepper(String value) { apiKeyPepper = value; }
    public long getApiKeyDefaultDays() { return apiKeyDefaultDays; }
    public void setApiKeyDefaultDays(long value) { apiKeyDefaultDays = value; }
}
