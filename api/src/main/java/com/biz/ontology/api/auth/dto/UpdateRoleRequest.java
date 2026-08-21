/**
 * 模块3：角色编辑API契约。
 * 功能：使用乐观锁保护修改角色名称；权限替换使用专用请求。
 * 技术栈：Java 17 record + Jakarta Bean Validation。
 */
package com.biz.ontology.api.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record UpdateRoleRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull @PositiveOrZero Long version
) {
}
