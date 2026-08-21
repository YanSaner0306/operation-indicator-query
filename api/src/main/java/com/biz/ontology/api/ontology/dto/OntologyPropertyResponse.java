package com.biz.ontology.api.ontology.dto;

import com.biz.ontology.common.enums.ConfigStatus;
import com.biz.ontology.ontology.enums.PropertyDataType;

import java.time.LocalDateTime;

public record OntologyPropertyResponse(
        Long id,
        Long ontologyId,
        String name,
        String code,
        PropertyDataType dataType,
        Integer length,
        Integer precision,
        Integer scale,
        boolean required,
        boolean uniqueFlag,
        String defaultValue,
        String description,
        Integer sortOrder,
        ConfigStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
