/**
 * 模块4：解析后的Access Token声明。
 * 功能：在过滤器与注销服务之间传递用户ID、jti、tokenVersion和到期时间。
 * 技术栈：Java 17不可变record。
 */
package com.biz.ontology.auth.token;

import java.time.Instant;

public record AccessTokenClaims(
        Long userId,
        String username,
        String jti,
        Long tokenVersion,
        Instant expiresAt
) {
}
