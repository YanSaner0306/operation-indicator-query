/**
 * 模块8：数据列元数据响应。
 * 功能：描述列类型、可空性和主键标志，为安全字段选择提供白名单。
 * 技术栈：Java 17 record与JDBC DatabaseMetaData。
 */
package com.biz.ontology.api.data.dto;

public record ColumnMetadataResponse(String name, String typeName, int jdbcType, boolean nullable, boolean primaryKey) {}
