/**
 * 模块3：管理员密码重置API契约。
 * 功能：接受新的受约束密码和乐观锁版本号，但绝不返回密码。
 * 技术栈：Java 17 record + Jakarta Bean Validation。
 */
package com.biz.ontology.api.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank @Size(min = 8, max = 72) String newPassword,
        @NotNull @PositiveOrZero Long version
) {
}
