/**
 * 模块14：API Key凭证实体。
 * 功能：仅保存keyId、可展示前缀、HMAC摘要、状态和到期时间，支持轮换与吊销。
 * 技术栈：Spring Data JPA与TokenStatus生命周期枚举。
 */
package com.biz.ontology.auth.apiclient.model;

import com.biz.ontology.auth.token.model.TokenStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name="api_key_credential")
public class ApiKeyCredentialEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="api_client_id",nullable=false) private Long apiClientId;
    @Column(name="key_id",nullable=false,unique=true,length=64) private String keyId;
    @Column(name="key_prefix",nullable=false,length=20) private String keyPrefix;
    @Column(name="secret_hash",nullable=false,length=255) private String secretHash;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private TokenStatus status;
    @Column(name="expires_at") private LocalDateTime expiresAt;
    @Column(name="last_used_at") private LocalDateTime lastUsedAt;
    @Column(name="created_at",nullable=false) private LocalDateTime createdAt;
    @Column(name="revoked_at") private LocalDateTime revokedAt;
    public Long getId(){return id;} public Long getApiClientId(){return apiClientId;} public void setApiClientId(Long v){apiClientId=v;}
    public String getKeyId(){return keyId;} public void setKeyId(String v){keyId=v;} public String getKeyPrefix(){return keyPrefix;} public void setKeyPrefix(String v){keyPrefix=v;}
    public String getSecretHash(){return secretHash;} public void setSecretHash(String v){secretHash=v;} public TokenStatus getStatus(){return status;} public void setStatus(TokenStatus v){status=v;}
    public LocalDateTime getExpiresAt(){return expiresAt;} public void setExpiresAt(LocalDateTime v){expiresAt=v;} public LocalDateTime getLastUsedAt(){return lastUsedAt;} public void setLastUsedAt(LocalDateTime v){lastUsedAt=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;} public LocalDateTime getRevokedAt(){return revokedAt;} public void setRevokedAt(LocalDateTime v){revokedAt=v;}
}
