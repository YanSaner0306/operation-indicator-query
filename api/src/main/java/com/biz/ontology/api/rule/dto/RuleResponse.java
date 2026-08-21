package com.biz.ontology.api.rule.dto;

import java.time.LocalDateTime;

public record RuleResponse(
        Long id,
        String name,
        String code,
        Long ontologyId,
        String ontologyName,
        String description,
        boolean enabled,
        Long currentVersionId,
        Integer currentVersionNo,
        RuleConditionResponse condition,
        RuleActionResponse action,
        Long version,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
