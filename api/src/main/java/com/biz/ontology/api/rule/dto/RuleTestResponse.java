package com.biz.ontology.api.rule.dto;

import com.biz.ontology.rule.enums.RuleOperator;

public record RuleTestResponse(
        Long ruleId,
        Integer versionNo,
        boolean matched,
        ConditionProcess condition,
        RuleActionResponse action
) {
    public record ConditionProcess(
            Long propertyId,
            String propertyName,
            Object actualValue,
            RuleOperator operator,
            String expectedValue
    ) {
    }
}
