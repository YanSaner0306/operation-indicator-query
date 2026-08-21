/**
 * 模块4：可持久化凭证的生命周期状态。
 * 功能：统一Refresh Token的有效、吊销和过期状态。
 * 技术栈：Java 17枚举，使用JPA字符串方式持久化。
 */
package com.biz.ontology.auth.token.model;

public enum TokenStatus {
    ACTIVE,
    REVOKED,
    EXPIRED
}
