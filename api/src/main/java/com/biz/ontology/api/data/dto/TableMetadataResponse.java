/**
 * 模块8：数据表元数据响应。
 * 功能：返回表名、类型和备注，不执行任何用户提供的SQL。
 * 技术栈：Java 17 record与JDBC DatabaseMetaData。
 */
package com.biz.ontology.api.data.dto;

public record TableMetadataResponse(String name, String type, String remarks) {}
