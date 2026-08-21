/**
 * 模块3：角色创建API契约。
 * 功能：验证稳定的全大写角色编码、名称以及权限字典选项。
 * 技术栈：Java 17 record + Jakarta Bean Validation。
 */
package com.biz.ontology.api.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CreateRoleRequest(
        @NotBlank @Size(max = 64) @Pattern(regexp = "[A-Z][A-Z0-9_]{2,63}") String code,
        @NotBlank @Size(max = 100) String name,
        Set<String> permissionCodes
) {
    public CreateRoleRequest {
        permissionCodes = permissionCodes == null ? Set.of() : Set.copyOf(permissionCodes);
    }
}
