/**
 * 模块7：数据源连接测试响应。
 * 功能：返回测试是否成功、耗时和不包含密码的诊断消息。
 * 技术栈：Java 17 record。
 */
package com.biz.ontology.api.data.dto;

public record ConnectionTestResponse(boolean success, long latencyMs, String message) {}
