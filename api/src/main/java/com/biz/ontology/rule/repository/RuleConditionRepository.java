package com.biz.ontology.rule.repository;

import com.biz.ontology.rule.model.RuleConditionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RuleConditionRepository extends JpaRepository<RuleConditionEntity, Long> {
    Optional<RuleConditionEntity> findByRuleVersionId(Long ruleVersionId);
    boolean existsByPropertyId(Long propertyId);

    @Query("""
            select count(condition)
            from RuleConditionEntity condition, RuleVersionEntity ruleVersion, RuleDefinitionEntity definition
            where condition.ruleVersionId = ruleVersion.id
              and ruleVersion.ruleId = definition.id
              and definition.deletedFlag = false
              and condition.propertyId = :propertyId
            """)
    long countActiveReferencesByPropertyId(@Param("propertyId") Long propertyId);
}
