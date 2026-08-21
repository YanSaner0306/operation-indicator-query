/**
 * 模块3：用户编辑API契约。
 * 功能：修改显示名称和角色分配，并进行乐观锁版本检查。
 * 技术栈：Java 17 record + Jakarta Bean Validation。
 */
package com.biz.ontology.api.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record UpdateUserRequest(
        @NotBlank @Size(max = 100) String displayName,
        Set<Long> roleIds,
        @NotNull @PositiveOrZero Long version
) {
    public UpdateUserRequest {
        roleIds = roleIds == null ? Set.of() : Set.copyOf(roleIds);
    }
}
