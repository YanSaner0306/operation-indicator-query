/**
 * 模块12：Binding启停请求契约。
 * 功能：通过目标状态和乐观锁版本保护启用、停用操作。
 * 技术栈：Java 17 record与Bean Validation。
 */
package com.biz.ontology.api.data.dto;

import com.biz.ontology.common.enums.ConfigStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateBindingStatusRequest(@NotNull ConfigStatus status,@NotNull Long version) {}
