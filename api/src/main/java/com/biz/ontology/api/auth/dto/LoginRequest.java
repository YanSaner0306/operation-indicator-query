/**
 * 模块4：后台用户登录请求。
 * 功能：校验用户名和密码的基本格式，具体认证由AuthenticationService完成。
 * 技术栈：Java 17 record与Jakarta Bean Validation。
 */
package com.biz.ontology.api.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank @Size(max = 100) String username,
        @NotBlank @Size(max = 72) String password
) {
}
