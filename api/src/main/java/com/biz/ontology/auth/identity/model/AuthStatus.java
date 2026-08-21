/**
 * 模块3：用户和角色共享的生命周期状态。
 * 功能：约束持久化和API可见的身份状态值。
 * 技术栈：Java 17枚举，由JPA以字符串形式持久化。
 */
package com.biz.ontology.auth.identity.model;

public enum AuthStatus {
    ENABLED,
    DISABLED
}
