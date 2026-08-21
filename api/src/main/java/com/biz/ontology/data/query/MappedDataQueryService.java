/**
 * 模块13：规则等业务模块使用的本体属性即时查询合同。
 * 功能：按本体、属性和业务唯一键读取单个映射值，调用方无需了解表名、字段名或数据库凭据。
 * 技术栈：Java Service接口与Optional结果语义。
 */
package com.biz.ontology.data.query;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface MappedDataQueryService {
    Optional<MappedValue> getPropertyValue(Long ontologyId,Long propertyId,Object businessKey);
    Optional<MappedRecord> getRecord(Long ontologyId,Object businessKey);
    List<MappedRecord> findRecords(Long ontologyId,Long propertyId,Object value,int limit);
    record MappedValue(Long bindingId,Long ontologyId,Long propertyId,Object businessKey,Object value){}
    record MappedRecord(Long bindingId,Long ontologyId,Object businessKey,Map<String,Object> values){}
}
