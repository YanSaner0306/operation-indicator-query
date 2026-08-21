/**
 * 模块3：安全用户响应契约。
 * 功能：暴露用户资料、状态、角色和版本信息，但排除密码哈希和登录机密。
 * 技术栈：Java 17 record，由Jackson序列化。
 */
package com.biz.ontology.api.auth.dto;

import com.biz.ontology.auth.identity.model.AuthStatus;

import java.time.LocalDateTime;
import java.util.Set;

public record UserResponse(
        Long id,
        String username,
        String displayName,
        AuthStatus status,
        Set<Long> roleIds,
        LocalDateTime lastLoginAt,
        LocalDateTime lockedUntil,
        Long version,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
