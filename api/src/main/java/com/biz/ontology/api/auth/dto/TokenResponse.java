/**
 * 模块4：Access Token签发响应。
 * 功能：只返回短期Access Token和主体摘要，Refresh Token通过HttpOnly Cookie传递。
 * 技术栈：Java 17 record与Spring MVC响应序列化。
 */
package com.biz.ontology.api.auth.dto;

public record TokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        CurrentPrincipalResponse principal
) {
}
