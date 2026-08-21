/**
 * 模块2、4、5：平台认证主体模型。
 * 功能：保存用户或API客户端身份、实时权限以及当前JWT的吊销校验信息。
 * 技术栈：Java 17 record，由Spring Security SecurityContext持有。
 */
package com.biz.ontology.common.security;

import java.util.Set;

public record PlatformPrincipal(
        String principalId,
        PrincipalType principalType,
        String displayName,
        Set<String> permissions,
        String jti,
        Long tokenVersion
) {
    public PlatformPrincipal {
        permissions = Set.copyOf(permissions);
    }

    public enum PrincipalType {
        USER,
        API_CLIENT
    }
}
