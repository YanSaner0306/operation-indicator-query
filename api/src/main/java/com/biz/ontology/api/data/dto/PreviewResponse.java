/**
 * 模块8：受限数据预览响应。
 * 功能：返回实际列顺序和最多100行脱敏数据。
 * 技术栈：Java集合、Jackson序列化与JDBC结果集映射。
 */
package com.biz.ontology.api.data.dto;

import java.util.List;
import java.util.Map;

public record PreviewResponse(List<String> columns, List<Map<String, Object>> rows, int limit) {}
