/**
 * 模块6：数据源启停请求契约。
 * 功能：以乐观锁版本保护启用和停用操作。
 * 技术栈：Java 17 record与Bean Validation。
 */
package com.biz.ontology.api.data.dto;

import com.biz.ontology.common.enums.ConfigStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateDataSourceStatusRequest(@NotNull ConfigStatus status, @NotNull Long version) {}
