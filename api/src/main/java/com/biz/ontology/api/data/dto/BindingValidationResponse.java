/**
 * 模块10：Binding校验结果响应。
 * 功能：返回校验是否通过及可供前端定位的结构化提示列表。
 * 技术栈：Java 17 record与集合序列化。
 */
package com.biz.ontology.api.data.dto;

import java.util.List;

public record BindingValidationResponse(boolean valid,List<String> messages) {}
