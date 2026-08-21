package com.biz.ontology.api.domain.dto;

import com.biz.ontology.domain.enums.DomainStatus;

import java.util.List;

public record DomainTreeNodeResponse(
        Long id,
        Long parentId,
        String name,
        String code,
        String description,
        DomainStatus status,
        Integer sortOrder,
        Long version,
        List<DomainTreeNodeResponse> children
) {
}
