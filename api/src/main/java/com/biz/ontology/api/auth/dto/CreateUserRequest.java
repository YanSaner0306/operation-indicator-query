/**
 * 模块3：用户创建API契约。
 * 功能：在服务执行前验证身份字段、初始密码和角色分配。
 * 技术栈：Java 17 record + Jakarta Bean Validation。
 */
package com.biz.ontology.api.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CreateUserRequest(
        @NotBlank @Size(max = 100) String username,
        @NotBlank @Size(max = 100) String displayName,
        @NotBlank @Size(min = 8, max = 72) String password,
        Set<Long> roleIds
) {
    public CreateUserRequest {
        roleIds = roleIds == null ? Set.of() : Set.copyOf(roleIds);
    }
}
