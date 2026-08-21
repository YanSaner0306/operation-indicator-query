/**
 * 模块3：用户/角色状态变更API契约。
 * 功能：携带期望的生命周期状态和乐观锁版本号。
 * 技术栈：Java 17 record + Jakarta Bean Validation。
 */
package com.biz.ontology.api.auth.dto;

import com.biz.ontology.auth.identity.model.AuthStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record UpdateAuthStatusRequest(
        @NotNull AuthStatus status,
        @NotNull @PositiveOrZero Long version
) {
}
