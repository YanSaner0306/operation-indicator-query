package com.biz.ontology.api.rule.dto;

import com.biz.ontology.rule.enums.RuleOperator;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RuleConditionRequest {
    @NotNull(message = "规则属性不能为空")
    private Long propertyId;

    @NotNull(message = "规则操作符不能为空")
    private RuleOperator operator;

    private Object compareValue;
}
