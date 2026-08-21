package com.biz.ontology.api.rule.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
public class RuleTestRequest {
    private Long versionId;

    @NotEmpty(message = "测试值不能为空")
    private Map<String, Object> values = new LinkedHashMap<>();
}
