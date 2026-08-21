/**
 * 模块14：API客户端安全响应契约。
 * 功能：返回机器身份、权限和凭证摘要，永不返回secret或secret哈希。
 * 技术栈：Java 17 record与Jackson序列化。
 */
package com.biz.ontology.api.auth.dto;
import com.biz.ontology.auth.identity.model.AuthStatus;
import com.biz.ontology.auth.token.model.TokenStatus;
import java.time.LocalDateTime;
import java.util.*;
public record ApiClientResponse(Long id,String clientId,String name,AuthStatus status,Set<String> permissionCodes,List<CredentialSummary> credentials,LocalDateTime lastUsedAt,Long version,LocalDateTime createdAt,LocalDateTime updatedAt){public record CredentialSummary(Long id,String keyId,String keyPrefix,TokenStatus status,LocalDateTime expiresAt,LocalDateTime lastUsedAt,LocalDateTime createdAt) {}}
