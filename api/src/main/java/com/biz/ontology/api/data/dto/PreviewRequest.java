/**
 * 模块8：数据预览请求契约。
 * 功能：只允许选择元数据中存在的列并将返回行数限制在1到100之间。
 * 技术栈：Java 17 record与Jakarta Bean Validation。
 */
package com.biz.ontology.api.data.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;

public record PreviewRequest(List<String> columns, @Min(1) @Max(100) Integer limit) {}
