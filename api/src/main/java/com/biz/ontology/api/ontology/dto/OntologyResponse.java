package com.biz.ontology.api.ontology.dto;

import com.biz.ontology.common.enums.ConfigStatus;

import java.time.LocalDateTime;
import java.util.List;

public record OntologyResponse(
        Long id,
        String name,
        String code,
        String description,
        ConfigStatus status,
        List<Long> domainIds,
        long propertyCount,
        long relationCount,
        long ruleCount,
        Long version,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
