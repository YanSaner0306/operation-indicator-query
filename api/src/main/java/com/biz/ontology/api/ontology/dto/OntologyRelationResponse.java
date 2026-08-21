package com.biz.ontology.api.ontology.dto;

import com.biz.ontology.common.enums.ConfigStatus;
import com.biz.ontology.ontology.enums.RelationCardinality;

import java.time.LocalDateTime;

public record OntologyRelationResponse(
        Long id,
        Long sourceOntologyId,
        Long targetOntologyId,
        String name,
        String code,
        RelationCardinality cardinality,
        Long sourcePropertyId,
        Long targetPropertyId,
        String description,
        ConfigStatus status,
        Long version,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
