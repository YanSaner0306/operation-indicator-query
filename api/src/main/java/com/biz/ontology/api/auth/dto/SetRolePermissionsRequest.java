/**
 * 模块3：角色-权限替换API契约。
 * 功能：仅接受后端字典代码以及当前角色版本。
 * 技术栈：Java 17 record + Jakarta Bean Validation。
 */
package com.biz.ontology.api.auth.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.Set;

public record SetRolePermissionsRequest(
        Set<String> permissionCodes,
        @NotNull @PositiveOrZero Long version
) {
    public SetRolePermissionsRequest {
        permissionCodes = permissionCodes == null ? Set.of() : Set.copyOf(permissionCodes);
    }
}
