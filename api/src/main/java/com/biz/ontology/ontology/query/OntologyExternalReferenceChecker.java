package com.biz.ontology.ontology.query;

public interface OntologyExternalReferenceChecker {
    boolean hasOntologyBinding(Long ontologyId);
    boolean hasPropertyBinding(Long propertyId);
}
