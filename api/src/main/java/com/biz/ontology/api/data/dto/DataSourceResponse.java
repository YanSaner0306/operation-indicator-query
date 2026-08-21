/**
 * 模块6：数据源安全响应契约。
 * 功能：返回可展示配置和测试状态，刻意排除密码、密文、IV及密钥版本。
 * 技术栈：Java 17 record与Jackson序列化。
 */
package com.biz.ontology.api.data.dto;

import com.biz.ontology.common.enums.ConfigStatus;
import com.biz.ontology.data.model.ConnectionTestStatus;
import com.biz.ontology.data.model.DatabaseType;
import java.time.LocalDateTime;

public record DataSourceResponse(Long id, String name, DatabaseType dbType, String host, Integer port,
                                 String databaseName, String username, ConfigStatus status,
                                 ConnectionTestStatus lastTestStatus, LocalDateTime lastTestAt,
                                 Long version, LocalDateTime createdAt, LocalDateTime updatedAt) {}
