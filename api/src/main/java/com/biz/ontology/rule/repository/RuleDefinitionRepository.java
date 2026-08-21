package com.biz.ontology.rule.repository;

import com.biz.ontology.rule.model.RuleDefinitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RuleDefinitionRepository extends JpaRepository<RuleDefinitionEntity, Long>, JpaSpecificationExecutor<RuleDefinitionEntity> {
    boolean existsByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);
    boolean existsByOntologyIdAndDeletedFlagFalse(Long ontologyId);
    long countByOntologyIdAndDeletedFlagFalse(Long ontologyId);
    boolean existsByIdAndDeletedFlagFalse(Long id);
}
