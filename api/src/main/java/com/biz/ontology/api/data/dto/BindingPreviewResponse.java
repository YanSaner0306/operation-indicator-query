/**
 * 模块11：Binding测试预览响应。
 * 功能：返回业务唯一键、物理字段结果和转换后的本体属性结果，固定最多一条记录。
 * 技术栈：Java 17 record、LinkedHashMap与Jackson。
 */
package com.biz.ontology.api.data.dto;

import java.util.Map;

public record BindingPreviewResponse(Long ontologyId,Object externalKey,Map<String,Object> sourceValues,Map<String,Object> properties,long durationMs) {}
