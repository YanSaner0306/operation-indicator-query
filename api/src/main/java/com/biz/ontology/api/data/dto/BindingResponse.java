/**
 * 模块9-12：Binding详情响应契约。
 * 功能：返回主配置、字段映射、筛选条件和测试状态，不暴露SQL或外部数据库凭据。
 * 技术栈：Java 17 record、Jackson和不可变集合。
 */
package com.biz.ontology.api.data.dto;

import com.biz.ontology.common.enums.ConfigStatus;
import com.biz.ontology.data.binding.model.BindingFilterOperator;
import com.biz.ontology.data.model.ConnectionTestStatus;
import java.time.LocalDateTime;
import java.util.List;

public record BindingResponse(Long id,String name,Long dataSourceId,String dataSourceName,String schemaName,String tableName,
                              Long ontologyId,String ontologyName,ConfigStatus status,ConnectionTestStatus lastTestStatus,
                              LocalDateTime lastTestAt,List<FieldMapping> mappings,List<FilterCondition> filters,
                              Long version,LocalDateTime createdAt,LocalDateTime updatedAt) {
    public record FieldMapping(Long id,String sourceColumn,String sourceDataType,Long ontologyPropertyId,String propertyCode,String propertyName,boolean uniqueKey) {}
    public record FilterCondition(Long id,String sourceColumn,String sourceDataType,BindingFilterOperator operator,String value,int sequence) {}
}
