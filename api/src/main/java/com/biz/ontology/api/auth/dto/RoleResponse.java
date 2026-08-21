/**
 * 模块3：角色响应契约。
 * 功能：返回角色状态、已分配的权限码、关联用户数和版本。
 * 技术栈：Java 17 record，由Jackson序列化。
 */
package com.biz.ontology.api.auth.dto;

import com.biz.ontology.auth.identity.model.AuthStatus;

import java.time.LocalDateTime;
import java.util.Set;

public record RoleResponse(
        Long id,
        String code,
        String name,
        AuthStatus status,
        Set<String> permissionCodes,
        long userCount,
        Long version,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
