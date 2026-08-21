/**
 * 模块6：创建或更新数据源的请求契约。
 * 功能：校验数据库类型、网络地址、库名、账号、密码和乐观锁版本。
 * 技术栈：Java 17 record与Jakarta Bean Validation。
 */
package com.biz.ontology.api.data.dto;

import com.biz.ontology.data.model.DatabaseType;
import jakarta.validation.constraints.*;

public record SaveDataSourceRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull DatabaseType dbType,
        @NotBlank @Size(max = 255) String host,
        @NotNull @Min(1) @Max(65535) Integer port,
        @NotBlank @Pattern(regexp = "[A-Za-z0-9_$-]+") @Size(max = 100) String databaseName,
        @NotBlank @Size(max = 100) String username,
        @Size(max = 500) String password,
        Long version
) {}
