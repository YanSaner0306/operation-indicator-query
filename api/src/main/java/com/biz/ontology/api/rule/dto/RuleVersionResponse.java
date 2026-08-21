package com.biz.ontology.api.rule.dto;

import java.time.LocalDateTime;

public record RuleVersionResponse(
        Long id,
        Integer versionNo,
        String changeNote,
        String createdBy,
        LocalDateTime createdAt,
        RuleConditionResponse condition,
        RuleActionResponse action
) {
}
