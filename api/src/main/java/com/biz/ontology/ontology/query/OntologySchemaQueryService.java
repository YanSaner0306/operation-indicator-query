package com.biz.ontology.ontology.query;

import java.util.List;

public interface OntologySchemaQueryService {
    List<OntologySummary> listEnabledOntologies(Long domainId);
    OntologySchema getOntologySchema(Long ontologyId);
    List<PropertyDefinition> listBindableProperties(Long ontologyId);
    MappingValidationResult validatePropertyMapping(Long propertyId, String sourceFieldType);
}
