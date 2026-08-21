/**
 * 模块9-10：创建或更新Binding的完整请求契约。
 * 功能：一次提交主配置、字段映射和AND筛选条件，后端重新读取元数据并完成全部校验。
 * 技术栈：Java 17 record、嵌套DTO与Jakarta Bean Validation。
 */
package com.biz.ontology.api.data.dto;

import com.biz.ontology.data.binding.model.BindingFilterOperator;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record SaveBindingRequest(
        @NotBlank @Size(max=100) String name,
        @NotNull Long dataSourceId,
        @Size(max=128) String schemaName,
        @NotBlank @Size(max=128) String tableName,
        @NotNull Long ontologyId,
        @NotEmpty List<@Valid FieldMappingItem> mappings,
        List<@Valid FilterItem> filters,
        Long version
) {
    public SaveBindingRequest { mappings=mappings==null?List.of():List.copyOf(mappings); filters=filters==null?List.of():List.copyOf(filters); }
    public record FieldMappingItem(@NotBlank @Size(max=128) String sourceColumn,@NotNull Long ontologyPropertyId) {}
    public record FilterItem(@NotBlank @Size(max=128) String sourceColumn,@NotNull BindingFilterOperator operator,@Size(max=2000) String value) {}
}
