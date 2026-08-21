package com.biz.ontology.ontology.query;

import com.biz.ontology.ontology.enums.PropertyDataType;

public record PropertyDefinition(
        Long id,
        String name,
        String code,
        PropertyDataType dataType,
        boolean required,
        boolean uniqueFlag
) {
}
