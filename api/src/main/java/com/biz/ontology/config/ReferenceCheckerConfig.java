package com.biz.ontology.config;

import com.biz.ontology.ontology.query.OntologyExternalReferenceChecker;
import com.biz.ontology.rule.service.RuleExternalReferenceChecker;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReferenceCheckerConfig {

    @Bean
    @ConditionalOnMissingBean(OntologyExternalReferenceChecker.class)
    public OntologyExternalReferenceChecker ontologyExternalReferenceChecker() {
        return new OntologyExternalReferenceChecker() {
            @Override
            public boolean hasOntologyBinding(Long ontologyId) {
                return false;
            }

            @Override
            public boolean hasPropertyBinding(Long propertyId) {
                return false;
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(RuleExternalReferenceChecker.class)
    public RuleExternalReferenceChecker ruleExternalReferenceChecker() {
        return ruleId -> false;
    }
}
