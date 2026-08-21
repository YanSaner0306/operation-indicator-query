package com.biz.ontology.ontology.query;

public interface OntologyReferenceQueryService {
    ReferenceSummary getOntologyReferences(Long ontologyId);
    ReferenceSummary getPropertyReferences(Long propertyId);
}
