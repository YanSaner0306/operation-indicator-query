package com.biz.ontology.ontology.query;

import com.biz.ontology.ontology.repository.OntologyPropertyRepository;
import com.biz.ontology.ontology.repository.OntologyRelationRepository;
import com.biz.ontology.rule.repository.RuleConditionRepository;
import com.biz.ontology.rule.repository.RuleDefinitionRepository;
import org.springframework.stereotype.Service;

@Service
public class OntologyReferenceQueryServiceImpl implements OntologyReferenceQueryService {

    private final OntologyPropertyRepository propertyRepository;
    private final OntologyRelationRepository relationRepository;
    private final RuleDefinitionRepository ruleRepository;
    private final RuleConditionRepository conditionRepository;
    private final OntologyExternalReferenceChecker externalReferenceChecker;

    public OntologyReferenceQueryServiceImpl(
            OntologyPropertyRepository propertyRepository,
            OntologyRelationRepository relationRepository,
            RuleDefinitionRepository ruleRepository,
            RuleConditionRepository conditionRepository,
            OntologyExternalReferenceChecker externalReferenceChecker) {
        this.propertyRepository = propertyRepository;
        this.relationRepository = relationRepository;
        this.ruleRepository = ruleRepository;
        this.conditionRepository = conditionRepository;
        this.externalReferenceChecker = externalReferenceChecker;
    }

    @Override
    public ReferenceSummary getOntologyReferences(Long ontologyId) {
        return new ReferenceSummary(
                propertyRepository.countByOntologyId(ontologyId),
                relationRepository.countBySourceOntologyIdOrTargetOntologyId(ontologyId, ontologyId),
                ruleRepository.countByOntologyIdAndDeletedFlagFalse(ontologyId),
                externalReferenceChecker.hasOntologyBinding(ontologyId) ? 1 : 0
        );
    }

    @Override
    public ReferenceSummary getPropertyReferences(Long propertyId) {
        return new ReferenceSummary(
                0,
                relationRepository.existsBySourcePropertyIdOrTargetPropertyId(propertyId, propertyId) ? 1 : 0,
                conditionRepository.countActiveReferencesByPropertyId(propertyId),
                externalReferenceChecker.hasPropertyBinding(propertyId) ? 1 : 0
        );
    }
}
