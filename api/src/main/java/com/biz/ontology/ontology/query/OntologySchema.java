package com.biz.ontology.ontology.query;

import java.util.List;

public record OntologySchema(
        Long id,
        String name,
        String code,
        List<PropertyDefinition> properties,
        PropertyDefinition uniqueProperty
) {
}
