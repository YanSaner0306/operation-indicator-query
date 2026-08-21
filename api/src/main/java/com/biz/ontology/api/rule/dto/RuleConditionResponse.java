package com.biz.ontology.api.rule.dto;

import com.biz.ontology.ontology.enums.PropertyDataType;
import com.biz.ontology.rule.enums.RuleOperator;

public record RuleConditionResponse(
        Long propertyId,
        String propertyName,
        RuleOperator operator,
        String compareValue,
        PropertyDataType valueType
) {
}
