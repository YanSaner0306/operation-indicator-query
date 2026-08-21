/**
 * 模块4-5：当前登录主体响应。
 * 功能：向前端返回最小身份信息和当前实时权限集合。
 * 技术栈：Java 17 record与Jackson序列化。
 */
package com.biz.ontology.api.auth.dto;

import java.util.Set;

public record CurrentPrincipalResponse(
        Long userId,
        String username,
        String displayName,
        Set<String> permissions
) {
}
