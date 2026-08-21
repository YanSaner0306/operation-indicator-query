package com.biz.ontology.api.domain.dto;

import com.biz.ontology.domain.enums.DomainStatus;

import java.time.LocalDateTime;

public record DomainResponse(
        Long id,
        Long parentId,
        String name,
        String code,
        String description,
        DomainStatus status,
        Integer sortOrder,
        Long version,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
