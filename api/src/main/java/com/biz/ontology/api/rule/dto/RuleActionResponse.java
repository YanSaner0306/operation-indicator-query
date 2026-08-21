package com.biz.ontology.api.rule.dto;

import com.biz.ontology.rule.enums.RuleActionType;

public record RuleActionResponse(
        RuleActionType actionType,
        String resultCode,
        String resultName,
        String message
) {
}
